package com.community.bitinstaller.utils

import android.content.Context
import com.community.bitinstaller.models.AppsConfig
import net.peanuuutz.tomlkt.Toml

class ConfigLoader(private val context: Context) {

    fun loadAppsConfig(): AppsConfig {
        val tomlString = context.assets.open("apps.toml").bufferedReader().use { it.readText() }
        
        try {
            val config = Toml.decodeFromString(AppsConfig.serializer(), tomlString)
            
            if (config.apps.isEmpty()) {
                throw IllegalStateException("No apps configured in apps.toml")
            }
            
            config.apps.forEach { app ->
                if (app.packageName.isBlank()) {
                    throw IllegalArgumentException("App '${app.appName}' has empty package_name")
                }
                if (app.appName.isBlank()) {
                    throw IllegalArgumentException("App with package '${app.packageName}' has empty app_name")
                }
                if (app.targetPath.isBlank()) {
                    throw IllegalArgumentException("App '${app.appName}' has empty target_path")
                }
                if (app.github.releaseTag.isBlank()) {
                    throw IllegalArgumentException("App '${app.appName}' has empty release_tag")
                }
                if (app.github.assetName.isBlank()) {
                    throw IllegalArgumentException("App '${app.appName}' has empty asset_name")
                }
                
                if (!InputValidator.validatePackageName(app.packageName)) {
                    throw IllegalArgumentException("App '${app.appName}' has invalid package_name: ${app.packageName}")
                }
                if (!InputValidator.validateTargetPath(app.targetPath)) {
                    throw IllegalArgumentException("App '${app.appName}' has invalid target_path: ${app.targetPath}")
                }
            }
            
            return config
        } catch (e: Exception) {
            throw IllegalStateException("Failed to parse apps.toml: ${e.message}", e)
        }
    }
}
