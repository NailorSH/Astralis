package com.nailorsh.astralis.features.home.impl.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.nailorsh.astralis.features.home.impl.presentation.data.model.BodyWithPosition

@Composable
actual fun SpaceScreen(
    planets: List<BodyWithPosition>,
    isOrientationTrackingOn: Boolean,
    modifier: Modifier
) {
    AndroidView(
        factory = { ctx ->
            SpaceView(ctx, planets).apply {
                updateSensorsState(
                    isOrientationTrackingOn
                )
            }
        },
        update = { view -> view.updateSensorsState(isOrientationTrackingOn) },
        modifier = modifier
    )
}