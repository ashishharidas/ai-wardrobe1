package com.example.api

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class VtonApiService {
    private val baseUrl = "https://gen.pollinations.ai"
    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private fun getFileFromUriOrUrl(context: Context, uriString: String, prefix: String): File? {
        if (uriString.startsWith("/")) {
            val f = File(uriString)
            if (f.exists()) return f
        }
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

    private fun getBase64Image(file: File): String {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.absolutePath, options)
        
        var scale = 1
        val maxSize = 512
        while (options.outWidth / scale / 2 >= maxSize && options.outHeight / scale / 2 >= maxSize) {
            scale *= 2
        }
        
        val decodeOptions = BitmapFactory.Options()
        decodeOptions.inSampleSize = scale
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
        
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    suspend fun generateTryOn(context: Context, bodyPhotoUriString: String, topImageUrl: String?, bottomImageUrl: String?): String? = withContext(Dispatchers.IO) {
        try {
            val bodyFile = getFileFromUriOrUrl(context, bodyPhotoUriString, "body") ?: return@withContext null
            
            val contentArray = JSONArray()
            val textObj = JSONObject()
            textObj.put("type", "text")
            
            val promptBuilder = java.lang.StringBuilder()
            promptBuilder.append("You are ÉLAN, a highly advanced virtual try-on assistant. ")
            promptBuilder.append("I have provided a photo of a person")
            if (topImageUrl != null) promptBuilder.append(", a top garment")
            if (bottomImageUrl != null) promptBuilder.append(", a bottom garment")
            promptBuilder.append(". Please generate a highly realistic image of this person wearing these exact clothes. ")
            promptBuilder.append("Output ONLY the image URL in markdown format like ![image](url).")
            
            textObj.put("text", promptBuilder.toString())
            contentArray.put(textObj)
            
            val bodyImgObj = JSONObject()
            bodyImgObj.put("type", "image_url")
            val bodyUrlObj = JSONObject()
            bodyUrlObj.put("url", "data:image/jpeg;base64,${getBase64Image(bodyFile)}")
            bodyImgObj.put("image_url", bodyUrlObj)
            contentArray.put(bodyImgObj)
            
            if (topImageUrl != null) {
                val topFile = getFileFromUriOrUrl(context, topImageUrl, "top")
                if (topFile != null) {
                    val topImgObj = JSONObject()
                    topImgObj.put("type", "image_url")
                    val topUrlObj = JSONObject()
                    topUrlObj.put("url", "data:image/jpeg;base64,${getBase64Image(topFile)}")
                    topImgObj.put("image_url", topUrlObj)
                    contentArray.put(topImgObj)
                }
            }
            
            if (bottomImageUrl != null) {
                val bottomFile = getFileFromUriOrUrl(context, bottomImageUrl, "bottom")
                if (bottomFile != null) {
                    val bottomImgObj = JSONObject()
                    bottomImgObj.put("type", "image_url")
                    val bottomUrlObj = JSONObject()
                    bottomUrlObj.put("url", "data:image/jpeg;base64,${getBase64Image(bottomFile)}")
                    bottomImgObj.put("image_url", bottomUrlObj)
                    contentArray.put(bottomImgObj)
                }
            }
            
            val messageObj = JSONObject()
            messageObj.put("role", "user")
            messageObj.put("content", contentArray)
            
            val messagesArray = JSONArray()
            messagesArray.put(messageObj)
            
            val payloadObj = JSONObject()
            payloadObj.put("model", "openai")
            payloadObj.put("messages", messagesArray)
            
            val requestBody = payloadObj.toString().toRequestBody("application/json".toMediaTypeOrNull())
            
            val request = Request.Builder()
                .url("$baseUrl/v1/chat/completions")
                .addHeader("Authorization", "Bearer YOUR_API_KEY_HERE")
                .post(requestBody)
                .build()
                
            val response = client.newCall(request).execute()
            val responseBodyString = response.body?.string()
            
            if (!response.isSuccessful) {
                throw Exception("Pollinations API error: ${response.code}")
            }
            
            if (responseBodyString == null) throw Exception("Empty response from server")
            
            val jsonObject = JSONObject(responseBodyString)
            val choices = jsonObject.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val message = choices.getJSONObject(0).optJSONObject("message")
                if (message != null) {
                    val content = message.optString("content", "")
                    
                    // Parse markdown image url
                    val matcher = Pattern.compile("!\\[.*?\\]\\((.*?)\\)").matcher(content)
                    var url = ""
                    if (matcher.find()) {
                        url = matcher.group(1) ?: ""
                    } else {
                        // try to find any http url
                        val httpMatcher = Pattern.compile("(https?://[^\\s]+)").matcher(content)
                        if (httpMatcher.find()) {
                            url = httpMatcher.group(1) ?: ""
                        }
                    }
                    
                    if (url.isNotEmpty()) {
                        val getRequest = Request.Builder().url(url).build()
                        val getResponse = client.newCall(getRequest).execute()
                        if (getResponse.isSuccessful) {
                            val bytes = getResponse.body?.bytes()
                            if (bytes != null) {
                                val genDir = StorageManager.getGeneratedDir(context)
                                val file = File(genDir, "gen_${System.currentTimeMillis()}.png")
                                file.writeBytes(bytes)
                                return@withContext file.absolutePath
                            }
                        }
                        return@withContext url
                    } else {
                        throw Exception("Failed to extract image from response: ${content.take(100)}")
                    }
                }
            }
            throw Exception("Invalid response format from Pollinations API")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
