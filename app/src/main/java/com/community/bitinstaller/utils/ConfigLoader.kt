package com.community.bitinstaller.utils

import android.content.Context
import com.community.bitinstaller.models.AppsConfig
import net.peanuuutz.tomlkt.Toml

class ConfigLoader(private val context: Context) {
    
    fun loadAppsConfig(): AppsConfig {
        val tomlString = context.assets.open("apps.toml").bufferedReader().use { it.readText() }
        return Toml.decodeFromString(AppsConfig.serializer(), tomlString)
    }
}
