package com.community.bitinstaller.utils

import android.content.Context
import com.community.bitinstaller.models.AppsConfig
import net.peanuuutz.tomlkt.Toml

class ConfigLoader(private val context: Context) {

    fun loadAppsConfig(): AppsConfig {
        val tomlString = context.assets.open("apps.toml").bufferedReader().use { it.readText() }

        try {
            val config = Toml.decodeFromString(AppsConfig.serializer(), tomlString)

            check(config.apps.isNotEmpty()) { "No apps configured in apps.toml" }

            config.apps.forEach { app ->
                require(app.packageName.isNotBlank()) {
                    "App '${app.appName}' has empty package_name"
                }
                require(app.appName.isNotBlank()) {
                    "App with package '${app.packageName}' has empty app_name"
                }
                require(app.targetPath.isNotBlank()) {
                    "App '${app.appName}' has empty target_path"
                }
                require(app.github.releaseTag.isNotBlank()) {
                    "App '${app.appName}' has empty release_tag"
                }
                require(app.github.assetName.isNotBlank()) {
                    "App '${app.appName}' has empty asset_name"
                }

                require(InputValidator.validatePackageName(app.packageName)) {
                    "App '${app.appName}' has invalid package_name: ${app.packageName}"
                }
                require(InputValidator.validateTargetPath(app.targetPath)) {
                    "App '${app.appName}' has invalid target_path: ${app.targetPath}"
                }
            }

            return config
        } catch (e: Exception) {
            throw IllegalStateException("Failed to parse apps.toml: ${e.message}", e)
        }
    }
}
