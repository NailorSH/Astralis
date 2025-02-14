package com.nailorsh.astralis.features.home.impl.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.kashif.cameraK.controller.CameraController
import com.kashif.cameraK.enums.CameraLens
import com.kashif.cameraK.enums.Directory
import com.kashif.cameraK.enums.FlashMode
import com.kashif.cameraK.enums.ImageFormat
import com.kashif.cameraK.ui.CameraPreview
import com.nailorsh.astralis.core.utils.cameraRequest
import com.nailorsh.astralis.core.utils.rememberCameraPermissionState

@Composable
actual fun CameraScreen(modifier: Modifier) {
    val cameraRequest = cameraRequest(
        onPermissionDenied = {
            /* TODO */
        }
    )

    val cameraPermissionState = rememberCameraPermissionState()

    LaunchedEffect(cameraPermissionState.value) {
        if (!cameraPermissionState.value) {
            cameraRequest()
        }
    }

    if (cameraPermissionState.value) {
        val cameraController = remember { mutableStateOf<CameraController?>(null) }

        CameraPreview(
            modifier = modifier,
            cameraConfiguration = {
                setCameraLens(CameraLens.BACK)
                setFlashMode(FlashMode.OFF)
                setImageFormat(ImageFormat.PNG)
                setDirectory(Directory.PICTURES)
            }, onCameraControllerReady = {
                cameraController.value = it
            }
        )
    }
}

