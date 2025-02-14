package com.nailorsh.astralis.features.home.impl.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.nailorsh.astralis.core.ui.components.LoadingBox
import com.nailorsh.astralis.features.home.impl.presentation.ui.components.HomeTopBar
import com.nailorsh.astralis.features.home.impl.presentation.ui.components.SpaceScreen
import com.nailorsh.astralis.features.home.impl.presentation.viewmodel.HomeState

@Composable
fun HomeScreen(
    state: HomeState,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        LoadingBox(
            modifier = modifier.fillMaxSize()
        )
    } else {
        HomeScreenContent(
            state = state,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun HomeScreenContent(
    state: HomeState,
    modifier: Modifier = Modifier
) {
    var isCameraOn by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            HomeTopBar(
                isCameraOn = isCameraOn,
                onCameraClicked = { isCameraOn = !isCameraOn },
            )
        },
        modifier = modifier
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = if (isCameraOn) 0.1f else 0.25f))
        ) {
            SpaceScreen(
                planets = state.planets,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
