package org.delcom.data

import kotlinx.serialization.Serializable
import org.delcom.entities.Animal

@Serializable
data class AnimalRequest(
    var nama: String = "",
    var deskripsi: String = "",
    var habitat: String = "",
    var makananFavorit: String = "",
    var pathGambar: String = "",
){
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "nama" to nama,
            "deskripsi" to deskripsi,
            "habitat" to habitat,
            "makananFavorit" to makananFavorit,
            "pathGambar" to pathGambar
        )
    }

    fun toEntity(): Animal {
        return Animal(
            nama = nama,
            deskripsi = deskripsi,
            habitat = habitat,
            makananFavorit = makananFavorit,
            pathGambar =  pathGambar,
        )
    }

}