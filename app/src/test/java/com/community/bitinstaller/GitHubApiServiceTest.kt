package com.community.bitinstaller

import com.community.bitinstaller.network.GitHubApiService
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GitHubApiServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var service: GitHubApiService

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        service = GitHubApiService("test/repo")
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    @Test
    fun `fetchReleases returns list on success`() = runTest {
        val json = """
            [{
                "tag_name": "v1.0.0",
                "body": "Release notes",
                "assets": [{
                    "name": "file.dat",
                    "browser_download_url": "https://example.com/file.dat"
                }]
            }]
        """.trimIndent()
        
        server.enqueue(MockResponse().setBody(json))
        
        val releases = service.fetchReleases()
        
        assertEquals(1, releases.size)
        assertEquals("v1.0.0", releases[0].tag_name)
    }

    @Test
    fun `fetchReleases throws on 404`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))
        
        try {
            service.fetchReleases()
            fail("Should have thrown exception")
        } catch (e: Exception) {
            assertTrue(e.message?.contains("not found") == true)
        }
    }

    @Test
    fun `fetchReleases throws on 403 rate limit`() = runTest {
        server.enqueue(MockResponse().setResponseCode(403))
        
        try {
            service.fetchReleases()
            fail("Should have thrown exception")
        } catch (e: Exception) {
            assertTrue(e.message?.contains("Rate limited") == true)
        }
    }
}
