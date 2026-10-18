package com.mnn.cameranote.screens.camera

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.mnn.cameranote.screens.camera.compoents.AlbumThumbnail
import com.mnn.cameranote.screens.camera.compoents.CaptureButton
import com.mnn.cameranote.screens.camera.compoents.CameraControls
import com.mnn.cameranote.screens.camera.compoents.NoteInputSheet
import org.koin.compose.viewmodel.koinViewModel
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onNavigateToGallery: () -> Unit,
    viewModel: CameraViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lastPhoto by viewModel.lastPhoto.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var retryKey by rememberSaveable { mutableIntStateOf(0) }

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    // 用来区分「没申请过」和「被永久拒绝」
    var requestedOnce by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            requestedOnce = true
            cameraPermission.launchPermissionRequest()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CameraEvent.Message -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (cameraPermission.status.isGranted) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                lensFacing = state.lensFacing,
                flashMode = state.flashMode,
                retryKey = retryKey,
                onImageCaptureReady = { imageCapture = it },
                onFlashAvailabilityChanged = viewModel::onFlashAvailabilityChanged,
                onCameraReady = viewModel::onCameraReady,
                onError = viewModel::onCameraError,
            )

            CameraControlsOverlay(
                state = state,
                lastPhoto = lastPhoto,
                onFlashClick = viewModel::onFlashToggle,
                onSwitchClick = viewModel::onLensSwitch,
                onCaptureClick = { viewModel.onCapture(imageCapture) },
                onNoteClick = viewModel::onOpenNoteSheet,
                onAlbumClick = onNavigateToGallery,
                onRetryClick = {
                    retryKey++
                    viewModel.onRetryCamera()
                },
            )
        } else {
            val permanentlyDenied = requestedOnce && !cameraPermission.status.shouldShowRationale
            PermissionDeniedContent(
                permanentlyDenied = permanentlyDenied,
                onRequestPermission = {
                    requestedOnce = true
                    cameraPermission.launchPermissionRequest()
                },
                onOpenSettings = { context.openAppSettings() },
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding(),
        )

        if (state.shutterFlashVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.45f)),
            )
        }
    }

    if (state.isNoteSheetVisible) {
        NoteInputSheet(
            note = state.noteDraft,
            itemCount = state.draftItemCount,
            onNoteChange = viewModel::onNoteDraftChange,
            onDismiss = viewModel::onCloseNoteSheet,
            onSave = viewModel::onSaveNote,
        )
    }
}

@Composable
private fun CameraControlsOverlay(
    state: CameraUiState,
    lastPhoto: File?,
    onFlashClick: () -> Unit,
    onSwitchClick: () -> Unit,
    onCaptureClick: () -> Unit,
    onNoteClick: () -> Unit,
    onAlbumClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        CameraControls(
            flashMode = state.flashMode,
            hasFlashUnit = state.hasFlashUnit,
            onFlashClick = onFlashClick,
            onSwitchClick = onSwitchClick,
        )

        state.cameraError?.let { message ->
            CameraErrorBanner(message = message, onRetry = onRetryClick)
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            AlbumThumbnail(
                photo = lastPhoto,
                onClick = onAlbumClick,
                modifier = Modifier.align(Alignment.CenterStart),
            )

            CaptureButton(
                isCapturing = state.isCapturing,
                enabled = state.canCapture,
                onClick = onCaptureClick,
                modifier = Modifier.align(Alignment.Center),
            )

            NoteButton(
                itemCount = state.draftItemCount,
                onClick = onNoteClick,
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
    }
}

@Composable
private fun CameraErrorBanner(message: String, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onRetry) {
                Text("重试")
            }
        }
    }
}

@Composable
private fun NoteButton(
    itemCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BadgedBox(
        badge = {
            if (itemCount > 0) {
                Badge { Text(itemCount.toString()) }
            }
        },
        modifier = modifier,
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f)),
        ) {
            Icon(
                imageVector = Icons.Default.EditNote,
                contentDescription = "写这条备忘的描述",
                tint = Color.White,
            )
        }
    }
}

private fun Context.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { startActivity(intent) }
}
