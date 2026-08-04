package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WardrobeDao {
    @Query("SELECT * FROM outfits")
    fun getAllOutfits(): Flow<List<Outfit>>

    @Query("SELECT * FROM outfits WHERE category = :category")
    fun getOutfitsByCategory(category: String): Flow<List<Outfit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutfit(outfit: Outfit)

    @Query("DELETE FROM outfits WHERE id = :id")
    suspend fun deleteOutfit(id: String)

    @Query("SELECT * FROM generated_looks ORDER BY timestamp DESC")
    fun getHistory(): Flow<List<GeneratedLook>>

    @Query("SELECT * FROM generated_looks WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<GeneratedLook>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLook(look: GeneratedLook)

    @Query("UPDATE generated_looks SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Int, isFavorite: Boolean)
}

