package com.prslc.myapplication

import android.net.Uri

enum class UpdatePriority {
    OPTIONAL,
    MANDATORY
}

data class UpdateConfig(
    val versionCode: Int,
    val versionName: String,
    val minSupportCode: Int,
    val downloadUrl: String,
    val releaseNotes: String,
    val sizeBytes: Long
)

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    
    data class Available(
        val config: UpdateConfig,
        val priority: UpdatePriority
    ) : UpdateState

    data class Downloading(
        val config: UpdateConfig,
        val progress: Float
    ) : UpdateState

    data class Ready(
        val config: UpdateConfig,
        val fileUri: Uri
    ) : UpdateState

    data class Failed(
        val reason: String
    ) : UpdateState
}
