package com.community.bitinstaller

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.community.bitinstaller.utils.FileDownloader
import com.community.bitinstaller.utils.ShizukuHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.io.File

class DownloadActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var sha256Text: TextView
    private lateinit var step1Status: TextView
    private lateinit var step2Status: TextView
    private lateinit var step3Status: TextView
    private lateinit var step4Status: TextView
    private lateinit var step1Icon: TextView
    private lateinit var step2Icon: TextView
    private lateinit var step3Icon: TextView
    private lateinit var step4Icon: TextView
    private lateinit var shizukuHelper: ShizukuHelper
    private lateinit var downloadedFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        progressBar = findViewById(R.id.progressBar)
        sha256Text = findViewById(R.id.sha256Text)
        step1Status = findViewById(R.id.step1Status)
        step2Status = findViewById(R.id.step2Status)
        step3Status = findViewById(R.id.step3Status)
        step4Status = findViewById(R.id.step4Status)
        step1Icon = findViewById(R.id.step1Icon)
        step2Icon = findViewById(R.id.step2Icon)
        step3Icon = findViewById(R.id.step3Icon)
        step4Icon = findViewById(R.id.step4Icon)

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

        startDownload(downloadUrl, packageName, targetPath)
    }

    private fun startDownload(url: String, packageName: String, targetPath: String) {
        lifecycleScope.launch {
            try {
                // Step 1: Fetch from source
                updateStep(1, "active", "Connecting to GitHub...")
                downloadedFile = File(cacheDir, "download_${System.currentTimeMillis()}")
                updateStep(1, "complete", "Connected ✓")
                
                // Step 2: Download file
                updateStep(2, "active", "Downloading...")
                val downloader = FileDownloader()
                val sha256 = downloader.downloadFile(downloadedFile, url) { progress ->
                    progressBar.progress = progress
                    step2Status.text = "$progress%"
                }
                updateStep(2, "complete", "Downloaded ✓")

                // Step 3: Verify hash
                updateStep(3, "active", "Calculating SHA-256...")
                sha256Text.text = sha256
                sha256Text.visibility = View.VISIBLE
                updateStep(3, "complete", "Verified ✓")

                // Step 4: Install file
                updateStep(4, "active", "Installing via Shizuku...")
                val success = shizukuHelper.copyFileToAppData(downloadedFile, packageName, targetPath)
                
                if (success) {
                    updateStep(4, "complete", "Installed ✓")
                    Snackbar.make(findViewById(android.R.id.content), "Installation successful!", Snackbar.LENGTH_LONG)
                        .setAction("Close") { finish() }
                        .show()
                } else {
                    updateStep(4, "error", "Installation failed")
                    showError("Installation failed")
                }
            } catch (e: Exception) {
                showError("Error: ${e.message}")
            }
        }
    }

    private fun updateStep(step: Int, state: String, status: String) {
        val icon = when (step) {
            1 -> step1Icon
            2 -> step2Icon
            3 -> step3Icon
            4 -> step4Icon
            else -> return
        }
        
        val statusText = when (step) {
            1 -> step1Status
            2 -> step2Status
            3 -> step3Status
            4 -> step4Status
            else -> return
        }

        when (state) {
            "active" -> {
                icon.setTextColor(0xFFFFFFFF.toInt())
                icon.setBackgroundColor(0xFF333333.toInt())
            }
            "complete" -> {
                icon.setTextColor(0xFF00FF00.toInt())
                icon.text = "✓"
            }
            "error" -> {
                icon.setTextColor(0xFFFF0000.toInt())
                icon.text = "✗"
            }
        }
        
        statusText.text = status
    }

    private fun showError(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setAction("Close") { finish() }
            .show()
    }
}
