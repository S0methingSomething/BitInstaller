package com.community.bitinstaller

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.community.bitinstaller.adapter.AppListAdapter
import com.community.bitinstaller.utils.ShizukuHelper
import com.community.bitinstaller.viewmodel.MainViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: AppListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var toolbar: MaterialToolbar
    private lateinit var shizukuHelper: ShizukuHelper
    private lateinit var shizukuStatusIcon: TextView
    private lateinit var shizukuStatusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        shizukuHelper = ShizukuHelper(this)
        
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
                shizukuHelper.requestPermission(1001)
                showError("Please grant Shizuku permission")
                return@AppListAdapter
            }
            
            val downloadUrl = viewModel.getDownloadUrl(appItem.config)
            if (downloadUrl == null) {
                showError("Download URL not found")
                return@AppListAdapter
            }
            
            val intent = Intent(this, DownloadActivity::class.java).apply {
                putExtra("DOWNLOAD_URL", downloadUrl)
                putExtra("PACKAGE_NAME", appItem.config.packageName)
                putExtra("TARGET_PATH", appItem.config.targetPath)
                putExtra("APP_NAME", appItem.config.appName)
            }
            startActivity(intent)
        }
        
        recyclerView.adapter = adapter

        swipeRefresh.setOnRefreshListener {
            viewModel.loadApps(this)
            updateShizukuStatus()
        }

        lifecycleScope.launch {
            viewModel.apps.collect { apps ->
                adapter.submitList(apps)
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

        viewModel.loadApps(this)
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
                shizukuStatusIcon.setTextColor(0xFFFF0000.toInt())
                shizukuStatusText.text = "Shizuku: Not Available"
            }
            !hasPermission -> {
                shizukuStatusIcon.setTextColor(0xFFFFAA00.toInt())
                shizukuStatusText.text = "Shizuku: Permission Required"
            }
            else -> {
                shizukuStatusIcon.setTextColor(0xFF00FF00.toInt())
                shizukuStatusText.text = "Shizuku: Ready"
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_change_source -> {
                showSourceDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSourceDialog() {
        val sources = arrayOf(
            "S0methingSomething/BitBot (Default)",
            "Custom GitHub Repository"
        )
        
        AlertDialog.Builder(this)
            .setTitle("Select Source")
            .setItems(sources) { _, which ->
                when (which) {
                    0 -> {
                        viewModel.setGitHubSource("S0methingSomething/BitBot")
                        viewModel.loadApps(this)
                    }
                    1 -> showCustomSourceDialog()
                }
            }
            .show()
    }

    private fun showCustomSourceDialog() {
        val input = android.widget.EditText(this)
        input.hint = "owner/repository"
        
        AlertDialog.Builder(this)
            .setTitle("Enter GitHub Repository")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val repo = input.text.toString().trim()
                if (repo.contains("/")) {
                    viewModel.setGitHubSource(repo)
                    viewModel.loadApps(this)
                } else {
                    showError("Invalid format. Use: owner/repository")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showError(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }
}
