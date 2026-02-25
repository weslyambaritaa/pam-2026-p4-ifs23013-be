package org.delcom.repositories

import org.delcom.entities.Animal

interface IAnimalRepository {
    suspend fun getAnimals(search: String): List<Animal>
    suspend fun getAnimalById(id: String): Animal?
    suspend fun getAnimalByName(name: String): Animal?
    suspend fun addAnimal(animal: Animal): String
    suspend fun updateAnimal(id: String, newAnimal: Animal): Boolean
    suspend fun removeAnimal(id: String): Boolean
}