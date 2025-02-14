package com.nailorsh.astralis.features.home.impl.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.nailorsh.astralis.core.ui.components.LoadingBox
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
            )
        },
        modifier = modifier
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = if (state.isCameraOn) 0.1f else 0.25f))
        ) {
            SpaceScreen(
                planets = state.planets,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
