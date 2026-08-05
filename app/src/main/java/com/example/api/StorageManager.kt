package com.example.api

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object StorageManager {

    fun getTopsDir(context: Context): File = File(context.filesDir, "tops").apply { mkdirs() }
    fun getBottomsDir(context: Context): File = File(context.filesDir, "bottoms").apply { mkdirs() }
    fun getGeneratedDir(context: Context): File = File(context.filesDir, "generated_images").apply { mkdirs() }
    fun getFavoritesDir(context: Context): File = File(context.filesDir, "favorites").apply { mkdirs() }

    fun saveInputStreamToDir(inputStream: InputStream, dir: File, prefix: String): File {
        val file = File(dir, "${prefix}_${UUID.randomUUID()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        return file
    }
    
    fun copyFileToFavorites(context: Context, sourcePath: String): String? {
        return try {
            val sourceFile = File(sourcePath)
            if (!sourceFile.exists()) return null
            
            val destFile = File(getFavoritesDir(context), sourceFile.name)
            sourceFile.copyTo(destFile, overwrite = true)
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
