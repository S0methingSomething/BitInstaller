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
    val isInstalled: Boolean,
    val installedVersion: String?,
    val availableVersion: String?
)

class MainViewModel : ViewModel() {
    private val _apps = MutableStateFlow<List<AppItem>>(emptyList())
    val apps: StateFlow<List<AppItem>> = _apps

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var apiService = GitHubApiService("S0methingSomething/BitBot")
    private var releases: List<GitHubRelease> = emptyList()

    fun setGitHubSource(repo: String) {
        apiService = GitHubApiService(repo)
    }

    private fun parseVersionFromDescription(body: String?, appName: String): String? {
        if (body == null) return null
        val lines = body.lines()
        var foundApp = false
        for (line in lines) {
            if (line.trim().startsWith("app:", ignoreCase = true)) {
                val app = line.substringAfter(":").trim()
                foundApp = app.equals(appName, ignoreCase = true)
            }
            if (foundApp && line.trim().startsWith("version:", ignoreCase = true)) {
                return line.substringAfter(":").trim()
            }
        }
        return null
    }

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
                    
                    val installedVersion = if (isInstalled) {
                        try {
                            pm.getPackageInfo(config.packageName, 0).versionName
                        } catch (e: Exception) {
                            null
                        }
                    } else null
                    
                    val release = releases.find { it.tag_name == config.github.releaseTag }
                    val availableVersion = release?.let { parseVersionFromDescription(it.body, config.appName) }
                    
                    AppItem(config, isInstalled, installedVersion, availableVersion)
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
