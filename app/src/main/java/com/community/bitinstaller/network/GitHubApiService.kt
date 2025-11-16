package com.community.bitinstaller.network

import com.community.bitinstaller.models.GitHubRelease
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class GitHubApiService(private val repository: String = "S0methingSomething/BitBot") {
    private val client = OkHttpClient()
    private val gson = Gson()

    suspend fun fetchReleases(): List<GitHubRelease> = withContext(Dispatchers.IO) {
        val apiUrl = "https://api.github.com/repos/$repository/releases"
        val request = Request.Builder()
            .url(apiUrl)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Failed to fetch releases: ${response.code}")
            val body = response.body?.string() ?: throw Exception("Empty response")
            val type = object : TypeToken<List<GitHubRelease>>() {}.type
            gson.fromJson(body, type)
        }
    }
}
