package com.example.chronos.data

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.IOException

object CloudinaryUploader {
    private const val CLOUD_NAME = "dto60yhqm"
    private const val UPLOAD_PRESET = "chronos_unsigned"
    private const val UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"

    fun uploadImage(file: File, callback: (Result<String>) -> Unit) {
        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, RequestBody.create("image/*".toMediaTypeOrNull(), file))
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .build()

        val request = Request.Builder()
            .url(UPLOAD_URL)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        callback(Result.failure(IOException("Unexpected code $it")))
                        return
                    }
                    val responseBody = it.body?.string()
                    val url = parseImageUrl(responseBody)
                    if (url != null) {
                        callback(Result.success(url))
                    } else {
                        callback(Result.failure(IOException("Failed to parse Cloudinary response")))
                    }
                }
            }
        })
    }

    private fun parseImageUrl(responseBody: String?): String? {
        val regex = Regex("""\"url\"\s*:\s*\"(.*?)\"""")
        return regex.find(responseBody ?: "")?.groups?.get(1)?.value
    }
} 