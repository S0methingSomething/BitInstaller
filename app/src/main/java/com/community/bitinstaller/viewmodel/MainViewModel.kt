package com.community.bitinstaller.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.community.bitinstaller.models.AppConfig
import com.community.bitinstaller.models.GitHubRelease
import com.community.bitinstaller.network.GitHubApiService
import com.community.bitinstaller.utils.ConfigLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AppItem(
    val config: AppConfig,
    val isInstalled: Boolean
)

class MainViewModel : ViewModel() {
    private val _apps = MutableStateFlow<List<AppItem>>(emptyList())
    val apps: StateFlow<List<AppItem>> = _apps

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val apiService = GitHubApiService()
    private var releases: List<GitHubRelease> = emptyList()

    fun loadApps(context: Context) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val configLoader = ConfigLoader(context)
                val appsConfig = configLoader.loadAppsConfig()
                releases = apiService.fetchReleases()
                
                val pm = context.packageManager
                _apps.value = appsConfig.apps.map { config ->
                    val isInstalled = try {
                        pm.getPackageInfo(config.packageName, 0)
                        true
                    } catch (e: PackageManager.NameNotFoundException) {
                        false
                    }
                    AppItem(config, isInstalled)
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _loading.value = false
            }
        }
    }

    fun getDownloadUrl(config: AppConfig): String? {
        val release = releases.find { it.tag_name == config.github.releaseTag } ?: return null
        val asset = release.assets.find { it.name == config.github.assetName } ?: return null
        return asset.browser_download_url
    }
}
