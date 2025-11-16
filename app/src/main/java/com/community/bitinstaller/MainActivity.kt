package com.community.bitinstaller

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.community.bitinstaller.adapter.AppListAdapter
import com.community.bitinstaller.utils.Constants
import com.community.bitinstaller.utils.InputValidator
import com.community.bitinstaller.utils.ShizukuHelper
import com.community.bitinstaller.viewmodel.MainViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var shizukuHelper: ShizukuHelper

    private lateinit var adapter: AppListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var toolbar: MaterialToolbar
    private lateinit var shizukuStatusIcon: TextView
    private lateinit var shizukuStatusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.recyclerView)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        progressBar = findViewById(R.id.progressBar)
        shizukuStatusIcon = findViewById(R.id.shizukuStatusIcon)
        shizukuStatusText = findViewById(R.id.shizukuStatusText)

        updateShizukuStatus()

        adapter = AppListAdapter { appItem ->
            if (!appItem.isInstalled) {
                showError("App not installed")
                return@AppListAdapter
            }

            if (!shizukuHelper.isShizukuAvailable()) {
                showError("Shizuku is not available. Please install and start Shizuku.")
                return@AppListAdapter
            }

            if (!shizukuHelper.checkPermission()) {
                shizukuHelper.requestPermission(Constants.RequestCodes.SHIZUKU_PERMISSION)
                showError("Please grant Shizuku permission")
                return@AppListAdapter
            }

            val downloadUrl = viewModel.getDownloadUrl(appItem.config)
            if (downloadUrl == null) {
                showError("Download URL not found")
                return@AppListAdapter
            }

            showConfirmationDialog(appItem, downloadUrl)
        }

        recyclerView.adapter = adapter

        swipeRefresh.setOnRefreshListener {
            viewModel.loadApps()
            updateShizukuStatus()
        }

        lifecycleScope.launch {
            viewModel.apps.collect { apps ->
                adapter.submitList(apps)
                findViewById<View>(R.id.emptyState).visibility =
                    if (apps.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.loading.collect { loading ->
                progressBar.visibility = if (loading) View.VISIBLE else View.GONE
                swipeRefresh.isRefreshing = loading
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let { showError(it) }
            }
        }

        viewModel.loadApps()
    }

    override fun onResume() {
        super.onResume()
        updateShizukuStatus()
    }

    private fun updateShizukuStatus() {
        val isAvailable = shizukuHelper.isShizukuAvailable()
        val hasPermission = shizukuHelper.checkPermission()

        when {
            !isAvailable -> {
                shizukuStatusIcon.setTextColor(ContextCompat.getColor(this, R.color.shizuku_unavailable))
                shizukuStatusText.text = "Shizuku: Not Available"
            }
            !hasPermission -> {
                shizukuStatusIcon.setTextColor(ContextCompat.getColor(this, R.color.shizuku_permission_required))
                shizukuStatusText.text = "Shizuku: Permission Required"
            }
            else -> {
                shizukuStatusIcon.setTextColor(ContextCompat.getColor(this, R.color.shizuku_available))
                shizukuStatusText.text = "Shizuku: Ready"
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_change_source) {
            showSourceDialog()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun showSourceDialog() {
        val sources = arrayOf(
            "${Constants.DEFAULT_GITHUB_REPO} (Default)",
            "Custom GitHub Repository"
        )

        AlertDialog.Builder(this)
            .setTitle("Select Source")
            .setItems(sources) { _, which ->
                when (which) {
                    0 -> {
                        viewModel.setGitHubSource(Constants.DEFAULT_GITHUB_REPO)
                        viewModel.loadApps()
                    }
                    1 -> showCustomSourceDialog()
                }
            }
            .show()
    }

    private fun showCustomSourceDialog() {
        val input = android.widget.EditText(this)
        input.hint = Constants.GITHUB_REPO_FORMAT

        AlertDialog.Builder(this)
            .setTitle("Enter GitHub Repository")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val repo = input.text.toString().trim()
                if (InputValidator.validateGitHubRepo(repo)) {
                    viewModel.setGitHubSource(repo)
                    viewModel.loadApps()
                } else {
                    showError("Invalid format. Use: ${Constants.GITHUB_REPO_FORMAT}")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showConfirmationDialog(appItem: com.community.bitinstaller.viewmodel.AppItem, downloadUrl: String) {
        AlertDialog.Builder(this)
            .setTitle("Install Configuration")
            .setMessage("Install configuration file to ${appItem.config.appName}?\n\nThis will modify app data.")
            .setPositiveButton("Install") { _, _ ->
                val intent = Intent(this, DownloadActivity::class.java).apply {
                    putExtra(Constants.IntentExtras.DOWNLOAD_URL, downloadUrl)
                    putExtra(Constants.IntentExtras.PACKAGE_NAME, appItem.config.packageName)
                    putExtra(Constants.IntentExtras.TARGET_PATH, appItem.config.targetPath)
                    putExtra(Constants.IntentExtras.APP_NAME, appItem.config.appName)
                    putExtra(Constants.IntentExtras.EXPECTED_SHA256, appItem.config.github.expectedSha256)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showError(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }
}
