package com.prslc.myapplication

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UpdateViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UpdateRepository()
    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state = _state.asStateFlow()

    private val currentVersionCode = 1
    private var downloadJob: Job? = null

    init {
        checkUpdates()
    }

    fun checkUpdates() {
        viewModelScope.launch {
            _state.value = UpdateState.Checking
            val result = repository.checkVersion()
            
            result.onSuccess { config ->
                if (config.versionCode > currentVersionCode) {
                    val priority = if (currentVersionCode < config.minSupportCode) {
                        UpdatePriority.MANDATORY
                    } else {
                        UpdatePriority.OPTIONAL
                    }
                    _state.value = UpdateState.Available(config, priority)
                } else {
                    _state.value = UpdateState.UpToDate
                }
            }.onFailure {
                _state.value = UpdateState.Failed("Network connection failed")
            }
        }
    }

    fun startDownload(config: UpdateConfig) {
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            repository.downloadApk(config.downloadUrl, getApplication())
                .collect { progress ->
                    if (progress >= 1f) {
                        val uri = repository.getFileUri(getApplication())
                        _state.value = UpdateState.Ready(config, uri)
                    } else {
                        _state.value = UpdateState.Downloading(config, progress)
                    }
                }
        }
    }

    fun installUpdate(uri: Uri) {
        val context = getApplication<Application>()
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            _state.value = UpdateState.Failed("Installer not found")
        }
    }

    fun dismiss() {
        _state.value = UpdateState.Idle
    }
}
