package com.community.bitinstaller.utils

object Constants {
    const val DEFAULT_GITHUB_REPO = "S0methingSomething/BitBot"
    const val GITHUB_REPO_FORMAT = "owner/repository"

    object IntentExtras {
        const val DOWNLOAD_URL = "DOWNLOAD_URL"
        const val PACKAGE_NAME = "PACKAGE_NAME"
        const val TARGET_PATH = "TARGET_PATH"
        const val APP_NAME = "APP_NAME"
        const val EXPECTED_SHA256 = "EXPECTED_SHA256"
    }

    object RequestCodes {
        const val SHIZUKU_PERMISSION = 1001
    }
}
