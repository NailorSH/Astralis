package com.nailorsh.astralis.features.home.impl.presentation.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults.iconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import astralis.composeapp.generated.resources.Res
import astralis.composeapp.generated.resources.ic_photo_camera_rounded
import org.jetbrains.compose.resources.vectorResource

@Composable
fun CameraButton2(modifier: Modifier = Modifier) {
    IconButton(
        onClick = {},
        colors = iconButtonColors(
            containerColor = Color.Black.copy(alpha = 0.25f),
            contentColor = Color.White,
        ),
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            imageVector = vectorResource(Res.drawable.ic_photo_camera_rounded),
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CameraButtonPreview() {
    MaterialTheme {
        CameraButton2()
    }
}