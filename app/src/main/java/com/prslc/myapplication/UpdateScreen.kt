package com.prslc.myapplication

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun UpdateScreenHost(viewModel: UpdateViewModel) {
    val state by viewModel.state.collectAsState()

    if (state is UpdateState.Idle || state is UpdateState.UpToDate) return

    val isMandatory = (state as? UpdateState.Available)?.priority == UpdatePriority.MANDATORY

    if (isMandatory) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            UpdateDialogContent(state, viewModel)
        }
    } else {
        Dialog(
            onDismissRequest = { viewModel.dismiss() },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            UpdateDialogContent(state, viewModel)
        }
    }
}

@Composable
private fun UpdateDialogContent(state: UpdateState, viewModel: UpdateViewModel) {
    AnimatedContent(
        targetState = state,
        transitionSpec = {
            scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn() togetherWith
            scaleOut() + fadeOut()
        },
        label = "UpdateAnimation"
    ) { target ->
        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (target) {
                    is UpdateState.Checking -> LoadingView()
                    is UpdateState.Available -> AvailableView(target, viewModel)
                    is UpdateState.Downloading -> DownloadingView(target)
                    is UpdateState.Ready -> ReadyView(target, viewModel)
                    is UpdateState.Failed -> ErrorView(target, viewModel)
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun HeaderIcon(icon: ImageVector, color: Color) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, modifier = Modifier.size(36.dp), tint = color)
    }
}

@Composable
private fun LoadingView() {
    CircularProgressIndicator()
    Text("Checking for updates...", style = MaterialTheme.typography.bodyLarge)
}

@Composable
private fun AvailableView(state: UpdateState.Available, viewModel: UpdateViewModel) {
    HeaderIcon(Icons.Rounded.RocketLaunch, MaterialTheme.colorScheme.primary)
    
    Text("New Version Available", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text("v${state.config.versionName}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = state.config.releaseNotes,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        if (state.priority == UpdatePriority.OPTIONAL) {
            TextButton(onClick = { viewModel.dismiss() }) { Text("Later") }
            Spacer(Modifier.width(8.dp))
        }
        Button(onClick = { viewModel.startDownload(state.config) }) {
            Text("Update Now")
        }
    }
}

@Composable
private fun DownloadingView(state: UpdateState.Downloading) {
    HeaderIcon(Icons.Rounded.CloudDownload, MaterialTheme.colorScheme.tertiary)
    Text("Downloading...", style = MaterialTheme.typography.titleLarge)
    
    LinearProgressIndicator(
        progress = { state.progress },
        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
    )
    Text("${(state.progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
}

@Composable
private fun ReadyView(state: UpdateState.Ready, viewModel: UpdateViewModel) {
    HeaderIcon(Icons.Rounded.SystemUpdate, MaterialTheme.colorScheme.primary)
    Text("Ready to Install", style = MaterialTheme.typography.headlineSmall)
    Button(
        onClick = { viewModel.installUpdate(state.fileUri) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Install & Restart")
    }
}

@Composable
private fun ErrorView(state: UpdateState.Failed, viewModel: UpdateViewModel) {
    HeaderIcon(Icons.Rounded.Warning, MaterialTheme.colorScheme.error)
    Text("Update Failed", style = MaterialTheme.typography.headlineSmall)
    Text(state.reason, style = MaterialTheme.typography.bodyMedium)
    Button(onClick = { viewModel.checkUpdates() }) { Text("Retry") }
}
