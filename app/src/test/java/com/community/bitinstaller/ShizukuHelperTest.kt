package com.community.bitinstaller

import com.community.bitinstaller.utils.ShizukuHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import rikka.shizuku.Shizuku

class ShizukuHelperTest {

    private lateinit var helper: ShizukuHelper

    @Before
    fun setup() {
        helper = ShizukuHelper()
        mockkStatic(Shizuku::class)
    }

    @Test
    fun `isShizukuAvailable returns true when binder responds`() {
        every { Shizuku.pingBinder() } returns true
        assertTrue(helper.isShizukuAvailable())
    }

    @Test
    fun `isShizukuAvailable returns false when exception thrown`() {
        every { Shizuku.pingBinder() } throws RuntimeException()
        assertFalse(helper.isShizukuAvailable())
    }

    @Test
    fun `copyFileToAppData throws SecurityException when no permission`() = runTest {
        every { Shizuku.checkSelfPermission() } returns -1

        try {
            helper.copyFileToAppData(mockk(), "com.test", "path")
            fail("Should have thrown SecurityException")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("permission") == true)
        }
    }

    @Test
    fun `copyFileToAppData throws SecurityException on invalid package name`() = runTest {
        every { Shizuku.checkSelfPermission() } returns 0

        try {
            helper.copyFileToAppData(mockk(), "invalid", "path")
            fail("Should have thrown SecurityException")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("Invalid") == true)
        }
    }
}
