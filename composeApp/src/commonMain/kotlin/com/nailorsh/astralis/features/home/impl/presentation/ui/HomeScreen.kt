package com.nailorsh.astralis.features.home.impl.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import com.nailorsh.astralis.core.ui.components.LoadingBox
import com.nailorsh.astralis.features.home.impl.presentation.ui.components.BackgroundBrightnessController
import com.nailorsh.astralis.features.home.impl.presentation.ui.components.CameraScreen
import com.nailorsh.astralis.features.home.impl.presentation.ui.components.HomeTopBar
import com.nailorsh.astralis.features.home.impl.presentation.ui.components.SpaceScreen
import com.nailorsh.astralis.features.home.impl.presentation.viewmodel.HomeAction
import com.nailorsh.astralis.features.home.impl.presentation.viewmodel.HomeState

@Composable
fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        LoadingBox(
            modifier = modifier.fillMaxSize()
        )
    } else {
        HomeScreenContent(
            state = state,
            onAction = onAction,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun HomeScreenContent(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var backgroundBrightness by remember { mutableFloatStateOf(1f) }

    Scaffold(
        topBar = {
            HomeTopBar(
                isCameraOn = state.isCameraOn,
                onCameraClicked = { onAction(HomeAction.OnCameraClicked) },
                isOrientationTrackingOn = state.isOrientationTrackingOn,
                onOrientationTrackingClicked = { onAction(HomeAction.OnOrientationTrackingClicked) }
            )
        },
        modifier = modifier
    ) {
        if (state.isCameraOn) {
            Box(modifier = modifier.fillMaxSize()) {
                CameraScreen(modifier = Modifier.fillMaxSize())
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(1 - backgroundBrightness)
                        .background(Color.Black)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(1 - backgroundBrightness)
                    .background(Color.Black)
            )
        }

        SpaceScreen(
            planets = state.planets,
            isOrientationTrackingOn = state.isOrientationTrackingOn,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight(0.3f).fillMaxWidth()
            ) {
                BackgroundBrightnessController(
                    value = backgroundBrightness,
                    onValueChange = { backgroundBrightness = it },
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
    }
}
