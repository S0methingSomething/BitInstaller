package com.community.bitinstaller.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * Handles file downloads with progress tracking and SHA-256 verification.
 */
class FileDownloader {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Downloads a file from a URL with optional SHA-256 verification.
     * @param outputFile The destination file
     * @param url The download URL
     * @param expectedSha256 Optional expected SHA-256 hash for verification
     * @param onProgress Callback for download progress (0-100)
     * @return The calculated SHA-256 hash of the downloaded file
     * @throws Exception if download fails
     * @throws SecurityException if SHA-256 verification fails
     */
    suspend fun downloadFile(
        outputFile: File,
        url: String,
        expectedSha256: String? = null,
        onProgress: (Int) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "BitInstaller-Android")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Download failed with code ${response.code}: ${response.message}")
            }

            val body = response.body ?: throw Exception("Empty response body")
            val contentLength = body.contentLength()

            outputFile.outputStream().use { output ->
                val input = body.byteStream()
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    if (contentLength > 0) {
                        val progress = (totalBytesRead * 100 / contentLength).toInt()
                        withContext(Dispatchers.Main) { onProgress(progress) }
                    }
                }
            }
        }

        val calculatedHash = calculateSHA256(outputFile)

        if (expectedSha256 != null && !calculatedHash.equals(expectedSha256, ignoreCase = true)) {
            outputFile.delete()
            throw SecurityException("SHA-256 verification failed. Expected: $expectedSha256, Got: $calculatedHash")
        }

        calculatedHash
    }

    /**
     * Calculates the SHA-256 hash of a file.
     * @param file The file to hash
     * @return The SHA-256 hash as a hexadecimal string
     */
    private fun calculateSHA256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
