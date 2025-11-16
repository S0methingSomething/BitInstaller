package com.community.bitinstaller.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.community.bitinstaller.utils.FileDownloader
import com.community.bitinstaller.utils.ShizukuHelper
import com.community.bitinstaller.utils.StorageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class DownloadState {
    object Idle : DownloadState()
    data class Connecting(val message: String) : DownloadState()
    data class Downloading(val progress: Int) : DownloadState()
    data class Verifying(val hash: String) : DownloadState()
    object Installing : DownloadState()
    object Success : DownloadState()
    data class Error(val message: String) : DownloadState()
}

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val fileDownloader: FileDownloader,
    private val shizukuHelper: ShizukuHelper
) : ViewModel() {

    private val _state = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val state: StateFlow<DownloadState> = _state

    fun startDownload(
        cacheDir: File,
        url: String,
        packageName: String,
        targetPath: String,
        expectedSha256: String?
    ) {
        viewModelScope.launch {
            var downloadedFile: File? = null
            try {
                if (!StorageUtils.hasEnoughSpace(cacheDir)) {
                    _state.value = DownloadState.Error("Insufficient storage space. Need at least 50MB free.")
                    return@launch
                }

                _state.value = DownloadState.Connecting("Connecting to GitHub...")
                downloadedFile = File(cacheDir, "download_${System.currentTimeMillis()}")

                _state.value = DownloadState.Downloading(0)
                val sha256 = fileDownloader.downloadFile(downloadedFile, url, expectedSha256) { progress ->
                    _state.value = DownloadState.Downloading(progress)
                }

                _state.value = DownloadState.Verifying(sha256)

                _state.value = DownloadState.Installing
                val success = shizukuHelper.copyFileToAppData(downloadedFile, packageName, targetPath)

                downloadedFile.delete()

                if (success) {
                    _state.value = DownloadState.Success
                } else {
                    _state.value = DownloadState.Error("Installation failed")
                }
            } catch (e: SecurityException) {
                downloadedFile?.delete()
                _state.value = DownloadState.Error("Security error: ${e.message}")
            } catch (e: java.io.IOException) {
                downloadedFile?.delete()
                _state.value = DownloadState.Error("Network error: ${e.message}")
            } catch (e: IllegalArgumentException) {
                downloadedFile?.delete()
                _state.value = DownloadState.Error("Invalid input: ${e.message}")
            } catch (e: Exception) {
                downloadedFile?.delete()
                _state.value = DownloadState.Error("Unexpected error: ${e.message}")
            }
        }
    }
}
