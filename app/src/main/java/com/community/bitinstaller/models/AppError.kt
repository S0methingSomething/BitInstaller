package com.community.bitinstaller.models

sealed class AppError(open val message: String) {
    data class NetworkError(val code: Int, override val message: String) : AppError(message)
    data class ParseError(override val message: String) : AppError(message)
    data class FileError(override val message: String) : AppError(message)
    data class ShizukuError(override val message: String) : AppError(message)
    data class ValidationError(val field: String, override val message: String) : AppError(message)
    data class SecurityError(override val message: String) : AppError(message)
    data class UnknownError(override val message: String) : AppError(message)
    
    fun toUserMessage(): String = when (this) {
        is NetworkError -> when (code) {
            404 -> "Release not found. Please check the source."
            403 -> "Rate limited. Please try again later."
            429 -> "Too many requests. Please wait a moment."
            in 500..599 -> "Server error. Please try again later."
            else -> "Network error occurred."
        }
        is ParseError -> "Invalid configuration format."
        is FileError -> "Download failed. Please check your connection."
        is ShizukuError -> "Installation failed. Check Shizuku permissions."
        is ValidationError -> "Invalid $field: $message"
        is SecurityError -> "Security verification failed."
        is UnknownError -> "An unexpected error occurred."
    }
}
