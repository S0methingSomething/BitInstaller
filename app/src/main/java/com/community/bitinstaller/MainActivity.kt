package com.community.bitinstaller

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.community.bitinstaller.adapter.AppListAdapter
import com.community.bitinstaller.viewmodel.MainViewModel
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: AppListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        recyclerView = findViewById(R.id.recyclerView)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        progressBar = findViewById(R.id.progressBar)

        adapter = AppListAdapter { appItem ->
            if (!appItem.isInstalled) {
                showError("App not installed")
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
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
