package com.community.bitinstaller.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    @SerialName("package_name")
    val packageName: String,
    @SerialName("app_name")
    val appName: String,
    @SerialName("target_path")
    val targetPath: String,
    val github: GithubConfig
)

@Serializable
data class GithubConfig(
    @SerialName("release_tag")
    val releaseTag: String,
    @SerialName("asset_name")
    val assetName: String,
    @SerialName("expected_sha256")
    val expectedSha256: String? = null
)

data class GitHubRelease(
    val tag_name: String,
    val body: String?,
    val assets: List<GitHubAsset>
)

data class GitHubAsset(
    val name: String,
    val browser_download_url: String
)

@Serializable
data class AppsConfig(
    val apps: List<AppConfig>
)
