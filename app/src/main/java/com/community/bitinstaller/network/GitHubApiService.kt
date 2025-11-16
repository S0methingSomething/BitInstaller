package com.community.bitinstaller.network

import com.community.bitinstaller.models.GitHubRelease
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.KeyStore
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class GitHubApiService(private val repository: String) {
    private val client: OkHttpClient by lazy {
        val systemTrustManager = getSystemTrustManager()
        val pinnedTrustManager = getPinnedTrustManager()
        val compositeTrustManager = CompositeX509TrustManager(systemTrustManager, pinnedTrustManager)

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(compositeTrustManager), SecureRandom())

        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .sslSocketFactory(sslContext.socketFactory, compositeTrustManager)
            .build()
    }

    private val gson = Gson()

    private fun getSystemTrustManager(): X509TrustManager {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(null as KeyStore?)
        return trustManagerFactory.trustManagers.first { it is X509TrustManager } as X509TrustManager
    }

    private fun getPinnedTrustManager(): X509TrustManager =
        getSystemTrustManager()

    suspend fun fetchReleases(): List<GitHubRelease> = withContext(Dispatchers.IO) {
        val apiUrl = "https://api.github.com/repos/$repository/releases"
        val request = Request.Builder()
            .url(apiUrl)
            .header("User-Agent", "BitInstaller-Android")
            .build()

        client.newCall(request).execute().use { response ->
            when (response.code) {
                200 -> {
                    val body = response.body?.string() ?: throw Exception("Empty response")
                    val type = object : TypeToken<List<GitHubRelease>>() {}.type
                    gson.fromJson<List<GitHubRelease>>(body, type)
                }
                404 -> throw Exception("Repository not found: $repository")
                403 -> throw Exception("Rate limited. Please try again later.")
                else -> throw Exception("Failed to fetch releases: ${response.code}")
            }
        }
    }
}
