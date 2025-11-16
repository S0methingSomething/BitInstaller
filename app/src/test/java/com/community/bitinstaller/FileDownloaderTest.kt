package com.community.bitinstaller

import com.community.bitinstaller.utils.FileDownloader
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class FileDownloaderTest {

    private lateinit var server: MockWebServer
    private lateinit var downloader: FileDownloader
    private lateinit var tempFile: File

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        downloader = FileDownloader()
        tempFile = File.createTempFile("test", ".tmp")
    }

    @After
    fun teardown() {
        server.shutdown()
        tempFile.delete()
    }

    @Test
    fun `downloadFile succeeds with valid response`() = runTest {
        val content = "test content"
        server.enqueue(MockResponse().setBody(content))

        val hash = downloader.downloadFile(tempFile, server.url("/test").toString()) {}

        assertEquals(content, tempFile.readText())
        assertNotNull(hash)
        assertEquals(64, hash.length)
    }

    @Test(expected = Exception::class)
    fun `downloadFile throws on 404`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))
        downloader.downloadFile(tempFile, server.url("/test").toString()) {}
    }

    @Test(expected = SecurityException::class)
    fun `downloadFile throws on hash mismatch`() = runTest {
        server.enqueue(MockResponse().setBody("test"))
        downloader.downloadFile(tempFile, server.url("/test").toString(), "invalid_hash") {}
    }

    @Test
    fun `downloadFile calls progress callback`() = runTest {
        val content = "x".repeat(1000)
        server.enqueue(MockResponse().setBody(content))

        val progressValues = mutableListOf<Int>()
        downloader.downloadFile(tempFile, server.url("/test").toString()) { progress ->
            progressValues.add(progress)
        }

        assertTrue(progressValues.isNotEmpty())
        assertTrue(progressValues.last() == 100)
    }
}
