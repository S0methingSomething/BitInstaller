package com.community.bitinstaller.utils

import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.File

/**
 * Helper class for Shizuku integration to perform privileged file operations.
 */
class ShizukuHelper {

    /**
     * Checks if Shizuku service is available and running.
     * @return true if Shizuku is available
     */
    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if the app has Shizuku permission.
     * @return true if permission is granted
     */
    fun checkPermission(): Boolean {
        return if (isShizukuAvailable()) {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
    }

    /**
     * Requests Shizuku permission from the user.
     * @param requestCode The request code for the permission callback
     */
    fun requestPermission(requestCode: Int) {
        if (isShizukuAvailable() && !checkPermission()) {
            Shizuku.requestPermission(requestCode)
        }
    }

    /**
     * Copies a file to another app's data directory using Shizuku privileges.
     * Validates paths to prevent security vulnerabilities.
     * @param sourceFile The file to copy
     * @param packageName The target app's package name
     * @param targetPath The relative path within the target app's directory
     * @return true if the copy succeeded
     * @throws SecurityException if permission is not granted or paths are invalid
     * @throws Exception if the copy operation fails
     */
    suspend fun copyFileToAppData(
        sourceFile: File,
        packageName: String,
        targetPath: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (!checkPermission()) throw SecurityException("Shizuku permission not granted")

        val validatedPath = InputValidator.validateAndSanitizePath(packageName, targetPath)
            ?: throw SecurityException("Invalid package name or target path")

        try {
            val command = arrayOf("cp", sourceFile.absolutePath, validatedPath)
            val process = Runtime.getRuntime().exec(command)
            val exitCode = process.waitFor()

            if (exitCode != 0) {
                val error = process.errorStream.bufferedReader().readText()
                throw Exception("Copy failed: $error")
            }

            exitCode == 0
        } catch (e: SecurityException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Failed to copy file: ${e.message}")
        }
    }
}
