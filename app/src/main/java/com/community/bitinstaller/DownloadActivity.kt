package com.community.bitinstaller

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.community.bitinstaller.utils.FileDownloader
import com.community.bitinstaller.utils.ShizukuHelper
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import java.io.File

class DownloadActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var sha256Label: TextView
    private lateinit var sha256Text: TextView
    private lateinit var sha256Card: View
    private lateinit var shizukuHelper: ShizukuHelper
    private lateinit var downloadedFile: File
    
    private val SHIZUKU_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        statusText = findViewById(R.id.statusText)
        progressBar = findViewById(R.id.progressBar)
        sha256Label = findViewById(R.id.sha256Label)
        sha256Text = findViewById(R.id.sha256Text)
        sha256Card = findViewById(R.id.sha256Card)

        shizukuHelper = ShizukuHelper(this)

        val downloadUrl = intent.getStringExtra("DOWNLOAD_URL") ?: run {
            showError("Missing download URL")
            return
        }
        val packageName = intent.getStringExtra("PACKAGE_NAME") ?: run {
            showError("Missing package name")
            return
        }
        val targetPath = intent.getStringExtra("TARGET_PATH") ?: run {
            showError("Missing target path")
            return
        }
        val appName = intent.getStringExtra("APP_NAME") ?: "App"

        Shizuku.addRequestPermissionResultListener(permissionListener)
        startDownload(downloadUrl, packageName, targetPath, appName)
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(permissionListener)
    }

    private val permissionListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        if (grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            val packageName = intent.getStringExtra("PACKAGE_NAME")!!
            val targetPath = intent.getStringExtra("TARGET_PATH")!!
            copyFileWithShizuku(packageName, targetPath)
        } else {
            showError("Shizuku permission denied")
        }
    }

    private fun startDownload(url: String, packageName: String, targetPath: String, appName: String) {
        lifecycleScope.launch {
            try {
                statusText.text = "Downloading..."
                downloadedFile = File(cacheDir, "download_${System.currentTimeMillis()}")
                
                val downloader = FileDownloader()
                val sha256 = downloader.downloadFile(downloadedFile, url) { progress ->
                    progressBar.progress = progress
                }

                statusText.text = "Download complete"
                sha256Card.visibility = View.VISIBLE
                sha256Text.text = sha256

                if (!shizukuHelper.isShizukuAvailable()) {
                    showError("Shizuku is not available")
                    return@launch
                }

                if (!shizukuHelper.checkPermission()) {
                    shizukuHelper.requestPermission(SHIZUKU_REQUEST_CODE)
                } else {
                    copyFileWithShizuku(packageName, targetPath)
                }
            } catch (e: Exception) {
                showError("Download failed: ${e.message}")
            }
        }
    }

    private fun copyFileWithShizuku(packageName: String, targetPath: String) {
        lifecycleScope.launch {
            try {
                statusText.text = "Installing..."
                val success = shizukuHelper.copyFileToAppData(downloadedFile, packageName, targetPath)
                
                if (success) {
                    showSuccess("Installation successful!")
                } else {
                    showError("Installation failed")
                }
            } catch (e: Exception) {
                showError("Installation failed: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> finish() }
            .show()
    }

    private fun showSuccess(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Success")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> finish() }
            .show()
    }
}
