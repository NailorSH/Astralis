package com.nailorsh.astralis.features.home.impl.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.nailorsh.astralis.core.ui.components.LoadingBox
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
        if (state.isCameraOn) CameraScreen(modifier = Modifier.fillMaxSize())
        else Box(modifier = modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)))

        SpaceScreen(
            planets = state.planets,
            isOrientationTrackingOn = state.isOrientationTrackingOn,
            modifier = Modifier.fillMaxSize()
        )
    }
}
