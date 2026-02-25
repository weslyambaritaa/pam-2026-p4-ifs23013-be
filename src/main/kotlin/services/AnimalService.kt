package org.delcom.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.data.AnimalRequest
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.IAnimalRepository
import java.io.File
import java.util.*

class AnimalService(private val animalRepository: IAnimalRepository) {
    // Mengambil semua data hewan
    suspend fun getAllAnimals(call: ApplicationCall) {
        val search = call.request.queryParameters["search"] ?: ""

        val animals = animalRepository.getAnimals(search)

        val response = DataResponse(
            "success",
            "Berhasil mengambil daftar hewan",
            mapOf(Pair("animals", animals))
        )
        call.respond(response)
    }

    // Mengambil data hewan berdasarkan id
    suspend fun getAnimalById(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID hewan tidak boleh kosong!")

        val animal = animalRepository.getAnimalById(id) ?: throw AppException(404, "Data hewan tidak tersedia!")

        val response = DataResponse(
            "success",
            "Berhasil mengambil data hewan",
            mapOf(Pair("animal", animal))
        )
        call.respond(response)
    }

    // Ambil data request dari multipart form data
    private suspend fun getAnimalRequest(call: ApplicationCall): AnimalRequest {
        val animalReq = AnimalRequest()

        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "nama" -> animalReq.nama = part.value.trim()
                        "deskripsi" -> animalReq.deskripsi = part.value
                        "habitat" -> animalReq.habitat = part.value
                        "makananFavorit" -> animalReq.makananFavorit = part.value
                    }
                }

                is PartData.FileItem -> {
                    val ext = part.originalFileName
                        ?.substringAfterLast('.', "")
                        ?.let { if (it.isNotEmpty()) ".$it" else "" }
                        ?: ""

                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/animals/$fileName"

                    val file = File(filePath)
                    file.parentFile.mkdirs()

                    part.provider().copyAndClose(file.writeChannel())
                    animalReq.pathGambar = filePath
                }

                else -> {}
            }
            part.dispose()
        }
        return animalReq
    }

    // Validasi data input hewan
    private fun validateAnimalRequest(animalReq: AnimalRequest){
        val validatorHelper = ValidatorHelper(animalReq.toMap())
        validatorHelper.required("nama", "Nama tidak boleh kosong")
        validatorHelper.required("deskripsi", "Deskripsi tidak boleh kosong")
        validatorHelper.required("habitat", "Habitat tidak boleh kosong")
        validatorHelper.required("makananFavorit", "Makanan Favorit tidak boleh kosong")
        validatorHelper.required("pathGambar", "Gambar tidak boleh kosong")
        validatorHelper.validate()

        val file = File(animalReq.pathGambar)
        if (!file.exists()) {
            throw AppException(400, "Gambar hewan gagal diupload!")
        }
    }

    // Menambahkan data hewan baru
    suspend fun createAnimal(call: ApplicationCall) {
        val animalReq = getAnimalRequest(call)
        validateAnimalRequest(animalReq)

        val existAnimal = animalRepository.getAnimalByName(animalReq.nama)
        if(existAnimal != null){
            val tmpFile = File(animalReq.pathGambar)
            if(tmpFile.exists()){
                tmpFile.delete()
            }
            throw AppException(409, "Hewan dengan nama ini sudah terdaftar!")
        }

        val animalId = animalRepository.addAnimal(
            animalReq.toEntity()
        )

        val response = DataResponse(
            "success",
            "Berhasil menambahkan data hewan",
            mapOf(Pair("animalId", animalId))
        )
        call.respond(response)
    }

    // Mengubah data hewan
    suspend fun updateAnimal(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID hewan tidak boleh kosong!")

        val oldAnimal = animalRepository.getAnimalById(id) ?: throw AppException(404, "Data hewan tidak tersedia!")

        val animalReq = getAnimalRequest(call)

        if(animalReq.pathGambar.isEmpty()){
            animalReq.pathGambar = oldAnimal.pathGambar
        }

        validateAnimalRequest(animalReq)

        if(animalReq.nama != oldAnimal.nama){
            val existAnimal = animalRepository.getAnimalByName(animalReq.nama)
            if(existAnimal != null){
                val tmpFile = File(animalReq.pathGambar)
                if(tmpFile.exists()){
                    tmpFile.delete()
                }
                throw AppException(409, "Hewan dengan nama ini sudah terdaftar!")
            }
        }

        if(animalReq.pathGambar != oldAnimal.pathGambar){
            val oldFile = File(oldAnimal.pathGambar)
            if(oldFile.exists()){
                oldFile.delete()
            }
        }

        val isUpdated = animalRepository.updateAnimal(
            id, animalReq.toEntity()
        )
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui data hewan!")
        }

        val response = DataResponse(
            "success",
            "Berhasil mengubah data hewan",
            null
        )
        call.respond(response)
    }

    // Menghapus data hewan
    suspend fun deleteAnimal(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID hewan tidak boleh kosong!")

        val oldAnimal = animalRepository.getAnimalById(id) ?: throw AppException(404, "Data hewan tidak tersedia!")

        val oldFile = File(oldAnimal.pathGambar)

        val isDeleted = animalRepository.removeAnimal(id)
        if (!isDeleted) {
            throw AppException(400, "Gagal menghapus data hewan!")
        }

        if (oldFile.exists()) {
            oldFile.delete()
        }

        val response = DataResponse(
            "success",
            "Berhasil menghapus data hewan",
            null
        )
        call.respond(response)
    }

    // Mengambil gambar hewan
    suspend fun getAnimalImage(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: return call.respond(HttpStatusCode.BadRequest)

        val animal = animalRepository.getAnimalById(id)
            ?: return call.respond(HttpStatusCode.NotFound)

        val file = File(animal.pathGambar)

        if (!file.exists()) {
            return call.respond(HttpStatusCode.NotFound)
        }

        call.respondFile(file)
    }
}