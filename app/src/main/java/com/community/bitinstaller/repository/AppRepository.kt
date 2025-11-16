package com.community.bitinstaller.repository

import com.community.bitinstaller.models.AppConfig
import com.community.bitinstaller.models.GitHubRelease

interface AppRepository {
    suspend fun loadAppsConfig(): List<AppConfig>
    suspend fun fetchReleases(repository: String): List<GitHubRelease>
}
