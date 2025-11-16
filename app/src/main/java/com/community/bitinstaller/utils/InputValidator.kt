package com.community.bitinstaller.utils

import java.io.File

/**
 * Utility object for validating user input to prevent security vulnerabilities.
 * Provides validation for package names, file paths, and GitHub repository identifiers.
 */
object InputValidator {
    private val PACKAGE_NAME_REGEX = Regex("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)+$")

    /**
     * Validates an Android package name format.
     * @param packageName The package name to validate
     * @return true if the package name matches Android naming conventions
     */
    fun validatePackageName(packageName: String): Boolean =
        PACKAGE_NAME_REGEX.matches(packageName)

    /**
     * Validates a target path to prevent path traversal attacks.
     * Rejects paths containing "..", absolute paths, and shell metacharacters.
     * @param targetPath The path to validate
     * @return true if the path is safe to use
     */
    fun validateTargetPath(targetPath: String): Boolean {
        if (targetPath.contains("..")) return false
        if (targetPath.startsWith("/")) return false
        if (targetPath.contains(";")) return false
        if (targetPath.contains("|")) return false
        if (targetPath.contains("&")) return false
        if (targetPath.contains("`")) return false
        if (targetPath.contains("$")) return false
        return true
    }

    /**
     * Validates and sanitizes a complete file path within an app's data directory.
     * Uses canonical path resolution to prevent directory traversal.
     * @param packageName The target app's package name
     * @param targetPath The relative path within the app's directory
     * @return The canonical path if valid, null otherwise
     */
    fun validateAndSanitizePath(packageName: String, targetPath: String): String? {
        if (!validatePackageName(packageName)) return null
        if (!validateTargetPath(targetPath)) return null

        val basePath = File("/data/data/$packageName")
        val targetFile = File(basePath, targetPath)

        return try {
            val canonicalPath = targetFile.canonicalPath
            if (!canonicalPath.startsWith(basePath.canonicalPath)) {
                null
            } else {
                canonicalPath
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Validates a GitHub repository identifier in "owner/repo" format.
     * @param repo The repository identifier to validate
     * @return true if the format is valid
     */
    fun validateGitHubRepo(repo: String): Boolean {
        val parts = repo.split("/")
        if (parts.size != 2) return false
        val (owner, repoName) = parts
        if (owner.isBlank() || repoName.isBlank()) return false
        if (owner.contains("..") || repoName.contains("..")) return false
        return true
    }
}
