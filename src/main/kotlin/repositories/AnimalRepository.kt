package org.delcom.repositories

import org.delcom.dao.AnimalDAO
import org.delcom.entities.Animal
import org.delcom.helpers.daoToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.AnimalTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import java.util.UUID

class AnimalRepository : IAnimalRepository {
    override suspend fun getAnimals(search: String): List<Animal> = suspendTransaction {
        if (search.isBlank()) {
            AnimalDAO.all()
                .orderBy(AnimalTable.createdAt to SortOrder.DESC)
                .limit(20)
                .map(::daoToModel)
        } else {
            val keyword = "%${search.lowercase()}%"

            AnimalDAO
                .find {
                    AnimalTable.nama.lowerCase() like keyword
                }
                .orderBy(AnimalTable.nama to SortOrder.ASC)
                .limit(20)
                .map(::daoToModel)
        }
    }

    override suspend fun getAnimalById(id: String): Animal? = suspendTransaction {
        AnimalDAO
            .find { (AnimalTable.id eq UUID.fromString(id)) }
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun getAnimalByName(name: String): Animal? = suspendTransaction {
        AnimalDAO
            .find { (AnimalTable.nama eq name) }
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun addAnimal(animal: Animal): String = suspendTransaction {
        val animalDAO = AnimalDAO.new {
            nama = animal.nama
            pathGambar = animal.pathGambar
            deskripsi = animal.deskripsi
            habitat = animal.habitat
            makananFavorit = animal.makananFavorit
            createdAt = animal.createdAt
            updatedAt = animal.updatedAt
        }

        animalDAO.id.value.toString()
    }

    override suspend fun updateAnimal(id: String, newAnimal: Animal): Boolean = suspendTransaction {
        val animalDAO = AnimalDAO
            .find { AnimalTable.id eq UUID.fromString(id) }
            .limit(1)
            .firstOrNull()

        if (animalDAO != null) {
            animalDAO.nama = newAnimal.nama
            animalDAO.pathGambar = newAnimal.pathGambar
            animalDAO.deskripsi = newAnimal.deskripsi
            animalDAO.habitat = newAnimal.habitat
            animalDAO.makananFavorit = newAnimal.makananFavorit
            animalDAO.updatedAt = newAnimal.updatedAt
            true
        } else {
            false
        }
    }

    override suspend fun removeAnimal(id: String): Boolean = suspendTransaction {
        val rowsDeleted = AnimalTable.deleteWhere {
            AnimalTable.id eq UUID.fromString(id)
        }
        rowsDeleted == 1
    }
}