package com.nailorsh.astralis.features.home.impl.presentation.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults.iconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import astralis.composeapp.generated.resources.Res
import astralis.composeapp.generated.resources.ic_photo_camera_rounded
import org.jetbrains.compose.resources.vectorResource

@Composable
fun CameraButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        colors = iconButtonColors(
            containerColor = Color.Black.copy(alpha = 0.25f),
            contentColor = MaterialTheme.colorScheme.primary,
        ),
        modifier = modifier
    ) {
        Icon(
            imageVector = vectorResource(Res.drawable.ic_photo_camera_rounded),
            contentDescription = null,
        )
    }
}
