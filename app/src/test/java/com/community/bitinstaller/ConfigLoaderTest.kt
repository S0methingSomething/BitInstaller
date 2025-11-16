package com.community.bitinstaller

import android.content.Context
import android.content.res.AssetManager
import com.community.bitinstaller.utils.ConfigLoader
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream

class ConfigLoaderTest {

    @Test
    fun `loadAppsConfig parses valid TOML`() {
        val validToml = """
            [[apps]]
            package_name = "com.example.app"
            app_name = "Test App"
            target_path = "files/config"

            [apps.github]
            release_tag = "v1.0.0"
            asset_name = "config.dat"
        """.trimIndent()

        val context = mockk<Context>()
        val assetManager = mockk<AssetManager>()
        every { context.assets } returns assetManager
        every { assetManager.open("apps.toml") } returns ByteArrayInputStream(validToml.toByteArray())

        val loader = ConfigLoader(context)
        val config = loader.loadAppsConfig()

        assertEquals(1, config.apps.size)
        assertEquals("com.example.app", config.apps[0].packageName)
        assertEquals("Test App", config.apps[0].appName)
        assertEquals("v1.0.0", config.apps[0].github.releaseTag)
    }

    @Test(expected = Exception::class)
    fun `loadAppsConfig throws on invalid TOML`() {
        val invalidToml = "invalid toml content"

        val context = mockk<Context>()
        val assetManager = mockk<AssetManager>()
        every { context.assets } returns assetManager
        every { assetManager.open("apps.toml") } returns ByteArrayInputStream(invalidToml.toByteArray())

        val loader = ConfigLoader(context)
        loader.loadAppsConfig()
    }
}
