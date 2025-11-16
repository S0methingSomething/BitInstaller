package com.community.bitinstaller.utils

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider
import java.io.File

class ShizukuHelper(private val context: Context) {

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    fun checkPermission(): Boolean {
        return if (isShizukuAvailable()) {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
    }

    fun requestPermission(requestCode: Int) {
        if (isShizukuAvailable() && !checkPermission()) {
            Shizuku.requestPermission(requestCode)
        }
    }

    suspend fun copyFileToAppData(
        sourceFile: File,
        packageName: String,
        targetPath: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (!checkPermission()) throw SecurityException("Shizuku permission not granted")
        
        val destination = "/data/data/$packageName/$targetPath"
        val command = "cp ${sourceFile.absolutePath} $destination"
        
        try {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            throw Exception("Failed to copy file: ${e.message}")
        }
    }
}
