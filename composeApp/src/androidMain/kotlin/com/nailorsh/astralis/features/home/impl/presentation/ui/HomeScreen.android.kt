package com.nailorsh.astralis.features.home.impl.presentation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.nailorsh.astralis.SpaceView
import com.nailorsh.astralis.features.home.impl.presentation.data.model.BodyWithPosition

@Composable
actual fun SpaceScreen(
    planets: List<BodyWithPosition>,
    modifier: Modifier
) {
    AndroidView(
        factory = { ctx -> SpaceView(ctx, planets) },
        modifier = Modifier.fillMaxSize()
    )
}