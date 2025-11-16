package com.community.bitinstaller

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.community.bitinstaller.viewmodel.MainViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: MainViewModel
    private lateinit var context: Context

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MainViewModel()
        context = mockk(relaxed = true)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setGitHubSource validates repo format`() {
        viewModel.setGitHubSource("invalid")
        assertNotNull(viewModel.error.value)
        
        viewModel.setGitHubSource("owner/repo")
        assertNull(viewModel.error.value)
    }

    @Test
    fun `loadApps sets loading state`() = runTest {
        val packageManager = mockk<PackageManager>()
        every { context.packageManager } returns packageManager
        every { context.assets } returns mockk(relaxed = true)
        
        assertFalse(viewModel.loading.value)
    }

    @Test
    fun `getDownloadUrl returns null when release not found`() {
        val config = mockk<com.community.bitinstaller.models.AppConfig>(relaxed = true)
        every { config.github.releaseTag } returns "nonexistent"
        
        val url = viewModel.getDownloadUrl(config)
        assertNull(url)
    }
}
