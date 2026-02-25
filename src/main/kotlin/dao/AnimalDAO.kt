package org.delcom.dao

import org.delcom.tables.AnimalTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.UUID

class AnimalDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, AnimalDAO>(AnimalTable)

    var nama by AnimalTable.nama
    var pathGambar by AnimalTable.pathGambar
    var deskripsi by AnimalTable.deskripsi
    var habitat by AnimalTable.habitat
    var makananFavorit by AnimalTable.makananFavorit
    var createdAt by AnimalTable.createdAt
    var updatedAt by AnimalTable.updatedAt
}