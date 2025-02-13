package com.nailorsh.astralis.features.home.impl.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.nailorsh.astralis.features.home.impl.presentation.ui.HomeScreen
import com.nailorsh.astralis.features.home.impl.presentation.viewmodel.MainViewModel

@Composable
fun CreateHomeScreen(viewModel: MainViewModel) {
    HomeScreen(
        state = viewModel.state.collectAsState().value,
    )
}