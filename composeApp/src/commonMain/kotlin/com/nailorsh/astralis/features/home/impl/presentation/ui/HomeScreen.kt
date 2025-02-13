package com.nailorsh.astralis.features.home.impl.presentation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    Scaffold(
        topBar = {
            HomeTopBar(
                onCameraClicked = { /* TODO */ },
            )
        },
        modifier = modifier
    ) {
        SpaceScreen(
            planets = state.planets,
            modifier = Modifier.fillMaxSize()
        )
    }
}
