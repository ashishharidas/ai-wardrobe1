package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "outfits")
data class Outfit(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val imageUrl: String,
    val category: String, // "Tops" or "Bottoms"
    val name: String
)

@Serializable
@Entity(tableName = "generated_looks")
data class GeneratedLook(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val topImageUrl: String? = null,
    val bottomImageUrl: String? = null,
    val resultImageUri: String,
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

