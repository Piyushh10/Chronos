package com.example.chronos.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

object AIGreetingApi {
    private val client = OkHttpClient()

    suspend fun fetchGreeting(prompt: String): String? = withContext(Dispatchers.IO) {
        val url = "https://text.pollinations.ai/prompt/${prompt.replace(" ", "+")}"
        val request = Request.Builder().url(url).build()
        return@withContext try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()
            } else null
        } catch (e: IOException) {
            null
        }
    }
} 