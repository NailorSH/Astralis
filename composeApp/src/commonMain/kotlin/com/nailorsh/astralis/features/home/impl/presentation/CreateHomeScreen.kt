package com.nailorsh.astralis.features.home.impl.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.nailorsh.astralis.features.home.impl.presentation.ui.HomeScreen
import com.nailorsh.astralis.features.home.impl.presentation.viewmodel.HomeViewModel

@Composable
fun CreateHomeScreen(viewModel: HomeViewModel) {
    HomeScreen(
        state = viewModel.state.collectAsState().value,
        onAction = viewModel::onAction
    )
}