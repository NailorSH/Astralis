package com.nailorsh.astralis.features.home.impl.presentation.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
private fun BrightnessControlPreview() {
    MaterialTheme {
        var sliderValue by remember { mutableFloatStateOf(0f) }

        ARLayerAlphaController(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            modifier = Modifier.height(200.dp)
        )
    }
}