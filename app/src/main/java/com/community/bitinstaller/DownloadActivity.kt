package com.community.bitinstaller

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.community.bitinstaller.utils.Constants
import com.community.bitinstaller.viewmodel.DownloadState
import com.community.bitinstaller.viewmodel.DownloadViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DownloadActivity : AppCompatActivity() {
    private val viewModel: DownloadViewModel by viewModels()

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

        val downloadUrl = intent.getStringExtra(Constants.IntentExtras.DOWNLOAD_URL) ?: run {
            showError("Missing download URL")
            return
        }
        val packageName = intent.getStringExtra(Constants.IntentExtras.PACKAGE_NAME) ?: run {
            showError("Missing package name")
            return
        }
        val targetPath = intent.getStringExtra(Constants.IntentExtras.TARGET_PATH) ?: run {
            showError("Missing target path")
            return
        }
        val expectedSha256 = intent.getStringExtra(Constants.IntentExtras.EXPECTED_SHA256)

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is DownloadState.Idle -> {}
                    is DownloadState.Connecting -> {
                        updateStep(1, "active", state.message)
                    }
                    is DownloadState.Downloading -> {
                        updateStep(1, "complete", "Connected ✓")
                        updateStep(2, "active", "${state.progress}%")
                        progressBar.progress = state.progress
                    }
                    is DownloadState.Verifying -> {
                        updateStep(2, "complete", "Downloaded ✓")
                        updateStep(3, "active", "Verifying...")
                        sha256Text.text = state.hash
                        sha256Text.visibility = View.VISIBLE
                        updateStep(3, "complete", "Verified ✓")
                    }
                    is DownloadState.Installing -> {
                        updateStep(4, "active", "Installing...")
                    }
                    is DownloadState.Success -> {
                        updateStep(4, "complete", "Installed ✓")
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Installation successful!",
                            Snackbar.LENGTH_LONG
                        ).setAction("Close") { finish() }
                            .show()
                    }
                    is DownloadState.Error -> {
                        updateStep(4, "error", "Failed")
                        showError(state.message)
                    }
                }
            }
        }

        viewModel.startDownload(cacheDir, downloadUrl, packageName, targetPath, expectedSha256)
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
                icon.setTextColor(ContextCompat.getColor(this, R.color.step_active))
                icon.setBackgroundColor(ContextCompat.getColor(this, R.color.step_active_bg))
            }
            "complete" -> {
                icon.setTextColor(ContextCompat.getColor(this, R.color.step_complete))
                icon.text = "✓"
            }
            "error" -> {
                icon.setTextColor(ContextCompat.getColor(this, R.color.step_error))
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
