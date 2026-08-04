package com.example.api

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class VtonApiService {
    val baseUrl = "http://192.168.220.36:8000/try-on"
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private fun getFileFromUriOrUrl(context: Context, uriString: String, prefix: String): File? {
        return try {
            val file = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.jpg")
            if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
                val request = Request.Builder().url(uriString).build()
                val response = OkHttpClient().newCall(request).execute()
                if (!response.isSuccessful) return null
                val bytes = response.body?.bytes() ?: return null
                file.writeBytes(bytes)
            } else {
                val uri = Uri.parse(uriString)
                val inputStream = context.contentResolver.openInputStream(uri) ?: return null
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun generateTryOn(context: Context, bodyPhotoUriString: String, topImageUrl: String?, bottomImageUrl: String?): String? = withContext(Dispatchers.IO) {
        try {
            val bodyFile = getFileFromUriOrUrl(context, bodyPhotoUriString, "body") ?: return@withContext null
            
            val multipartBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("person_image", bodyFile.name, bodyFile.asRequestBody("image/*".toMediaTypeOrNull()))

            if (topImageUrl != null) {
                val topFile = getFileFromUriOrUrl(context, topImageUrl, "top")
                if (topFile != null) {
                    multipartBuilder.addFormDataPart("top_image", topFile.name, topFile.asRequestBody("image/*".toMediaTypeOrNull()))
                }
            }

            if (bottomImageUrl != null) {
                val bottomFile = getFileFromUriOrUrl(context, bottomImageUrl, "bottom")
                if (bottomFile != null) {
                    multipartBuilder.addFormDataPart("bottom_image", bottomFile.name, bottomFile.asRequestBody("image/*".toMediaTypeOrNull()))
                }
            }

            val requestBody = multipartBuilder.build()

            val request = Request.Builder()
                .url(baseUrl)
                .addHeader("ngrok-skip-browser-warning", "true")
                .addHeader("User-Agent", "ELAN-Android/1.0")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBodyString = response.body?.string()

            if (!response.isSuccessful) {
                if (response.code == 429) {
                    val limitMsg = try {
                        JSONObject(responseBodyString ?: "").optString("message", "API Limit Exceeded")
                    } catch (e: Exception) {
                        "API Limit Exceeded"
                    }
                    throw Exception(limitMsg)
                }
                throw Exception("Backend server error: ${response.code}")
            }

            if (responseBodyString == null) throw Exception("Empty response from server")
            
            val jsonObject = JSONObject(responseBodyString)
            if (jsonObject.has("limit_exceeded") && jsonObject.getBoolean("limit_exceeded")) {
                 throw Exception(jsonObject.optString("message", "API Limit Exceeded"))
            }
            
            val url = jsonObject.optString("output_url")
            if (url.isNotEmpty()) {
                return@withContext url
            } else {
                throw Exception("Failed to generate image. Try again.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}

