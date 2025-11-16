package com.community.bitinstaller.repository

import com.community.bitinstaller.models.AppConfig
import com.community.bitinstaller.models.GitHubRelease
import com.community.bitinstaller.network.GitHubApiService
import com.community.bitinstaller.utils.ConfigLoader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepositoryImpl @Inject constructor(
    private val configLoader: ConfigLoader,
    private val apiServiceFactory: GitHubApiServiceFactory
) : AppRepository {

    override suspend fun loadAppsConfig(): List<AppConfig> {
        return configLoader.loadAppsConfig().apps
    }

    override suspend fun fetchReleases(repository: String): List<GitHubRelease> {
        val apiService = apiServiceFactory.create(repository)
        return apiService.fetchReleases()
    }
}

interface GitHubApiServiceFactory {
    fun create(repository: String): GitHubApiService
}

@Singleton
class GitHubApiServiceFactoryImpl @Inject constructor() : GitHubApiServiceFactory {
    override fun create(repository: String): GitHubApiService {
        return GitHubApiService(repository)
    }
}
