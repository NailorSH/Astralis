package com.nailorsh.astralis.features.home.impl.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nailorsh.astralis.core.ui.components.LoadingBox
import com.nailorsh.astralis.features.home.impl.presentation.data.model.BodyWithPosition
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
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        SpaceScreen(state.planets)

        Button(
            onClick = { /* Действие */ },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
        ) {
            Text("Нажми меня")
        }
    }
}

@Composable
expect fun SpaceScreen(
    planets: List<BodyWithPosition>,
    modifier: Modifier = Modifier
)