package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object AnimalTable : UUIDTable("animals") {
    val nama = varchar("nama", 100)
    val pathGambar = varchar("path_gambar", 255)
    val deskripsi = text("deskripsi")
    val habitat = varchar("habitat", 100)
    val makananFavorit = varchar("makanan_favorit", 100)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}