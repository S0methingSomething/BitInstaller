package com.community.bitinstaller

import com.community.bitinstaller.utils.InputValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InputValidatorTest {

    @Test
    fun `validatePackageName accepts valid package names`() {
        assertTrue(InputValidator.validatePackageName("com.example.app"))
        assertTrue(InputValidator.validatePackageName("com.company.product.feature"))
        assertTrue(InputValidator.validatePackageName("a.b"))
    }

    @Test
    fun `validatePackageName rejects invalid package names`() {
        assertFalse(InputValidator.validatePackageName(""))
        assertFalse(InputValidator.validatePackageName("com"))
        assertFalse(InputValidator.validatePackageName(".com.example"))
        assertFalse(InputValidator.validatePackageName("com.example."))
        assertFalse(InputValidator.validatePackageName("com..example"))
        assertFalse(InputValidator.validatePackageName("com.example; rm -rf /"))
    }

    @Test
    fun `validateTargetPath accepts valid paths`() {
        assertTrue(InputValidator.validateTargetPath("files/config"))
        assertTrue(InputValidator.validateTargetPath("data/settings.xml"))
        assertTrue(InputValidator.validateTargetPath("cache"))
    }

    @Test
    fun `validateTargetPath rejects path traversal attempts`() {
        assertFalse(InputValidator.validateTargetPath("../etc/passwd"))
        assertFalse(InputValidator.validateTargetPath("files/../../system"))
        assertFalse(InputValidator.validateTargetPath("/absolute/path"))
    }

    @Test
    fun `validateTargetPath rejects shell metacharacters`() {
        assertFalse(InputValidator.validateTargetPath("file;rm -rf /"))
        assertFalse(InputValidator.validateTargetPath("file|cat"))
        assertFalse(InputValidator.validateTargetPath("file&whoami"))
        assertFalse(InputValidator.validateTargetPath("file`id`"))
        assertFalse(InputValidator.validateTargetPath("file\$HOME"))
    }

    @Test
    fun `validateGitHubRepo accepts valid repos`() {
        assertTrue(InputValidator.validateGitHubRepo("owner/repo"))
        assertTrue(InputValidator.validateGitHubRepo("user123/project-name"))
    }

    @Test
    fun `validateGitHubRepo rejects invalid repos`() {
        assertFalse(InputValidator.validateGitHubRepo(""))
        assertFalse(InputValidator.validateGitHubRepo("owner"))
        assertFalse(InputValidator.validateGitHubRepo("owner/"))
        assertFalse(InputValidator.validateGitHubRepo("/repo"))
        assertFalse(InputValidator.validateGitHubRepo("owner/repo/extra"))
        assertFalse(InputValidator.validateGitHubRepo("owner/../repo"))
    }
}
