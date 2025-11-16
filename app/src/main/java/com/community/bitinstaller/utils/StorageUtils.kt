package com.community.bitinstaller.utils

import android.os.StatFs
import java.io.File

object StorageUtils {
    private const val MIN_FREE_SPACE_MB = 50L

    fun hasEnoughSpace(directory: File, requiredBytes: Long = MIN_FREE_SPACE_MB * 1024 * 1024): Boolean {
        val stat = StatFs(directory.path)
        val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
        return availableBytes >= requiredBytes
    }

    fun getAvailableSpaceMB(directory: File): Long {
        val stat = StatFs(directory.path)
        val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
        return availableBytes / (1024 * 1024)
    }
}
