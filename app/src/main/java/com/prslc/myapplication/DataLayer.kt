package com.prslc.myapplication

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class UpdateRepository {
    
    suspend fun checkVersion(): Result<UpdateConfig> {
        delay(1500)
        return Result.success(
            UpdateConfig(
                versionCode = 5,
                versionName = "2.1.0",
                minSupportCode = 2,
                downloadUrl = "https://mock.com/update.apk",
                releaseNotes = "• Security Patch\n• New Dashboard\n• Performance Fixes",
                sizeBytes = 15_000_000
            )
        )
    }

    fun downloadApk(url: String, context: Context): Flow<Float> = flow {
        var progress = 0f
        while (progress < 1f) {
            delay(100)
            progress += 0.02f
            emit(progress.coerceAtMost(1f))
        }
    }

    fun getFileUri(context: Context): Uri {
        val file = File(context.cacheDir, "update.apk")
        if (!file.exists()) file.createNewFile()
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
}
