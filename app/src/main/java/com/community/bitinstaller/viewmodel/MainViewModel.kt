package com.community.bitinstaller.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.community.bitinstaller.models.AppConfig
import com.community.bitinstaller.models.GitHubRelease
import com.community.bitinstaller.repository.AppRepository
import com.community.bitinstaller.utils.Constants
import com.community.bitinstaller.utils.InputValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppItem(
    val config: AppConfig,
    val isInstalled: Boolean,
    val installedVersion: String?,
    val availableVersion: String?
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: AppRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _apps = MutableStateFlow<List<AppItem>>(
        savedStateHandle.get<List<AppItem>>("apps") ?: emptyList()
    )
    val apps: StateFlow<List<AppItem>> = _apps

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var currentRepo = savedStateHandle.get<String>("repo") ?: Constants.DEFAULT_GITHUB_REPO
    private var releases: List<GitHubRelease> = emptyList()

    fun setGitHubSource(repo: String) {
        if (!InputValidator.validateGitHubRepo(repo)) {
            _error.value = "Invalid GitHub repository format"
            return
        }
        currentRepo = repo
        savedStateHandle["repo"] = repo
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

    fun loadApps() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val appsConfig = repository.loadAppsConfig()
                releases = repository.fetchReleases(currentRepo)

                val pm = context.packageManager
                val appItems = appsConfig.map { config ->
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
                    } else {
                        null
                    }

                    val release = releases.find { it.tagName == config.github.releaseTag }
                    val availableVersion = release?.let { parseVersionFromDescription(it.body, config.appName) }

                    AppItem(config, isInstalled, installedVersion, availableVersion)
                }

                _apps.value = appItems
                savedStateHandle["apps"] = appItems
            } catch (e: IllegalStateException) {
                _error.value = "Configuration error: ${e.message}"
            } catch (e: IllegalArgumentException) {
                _error.value = "Invalid configuration: ${e.message}"
            } catch (e: java.io.IOException) {
                _error.value = "Network error: ${e.message}"
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun getDownloadUrl(config: AppConfig): String? {
        val release = releases.find { it.tagName == config.github.releaseTag } ?: return null
        val asset = release.assets.find { it.name == config.github.assetName } ?: return null
        return asset.browserDownloadUrl
    }
}
