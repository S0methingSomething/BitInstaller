package com.community.bitinstaller

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.community.bitinstaller.adapter.AppListAdapter
import com.community.bitinstaller.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: AppListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
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

    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
