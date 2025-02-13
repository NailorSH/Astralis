package com.nailorsh.astralis.features.home.impl.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nailorsh.astralis.features.home.impl.presentation.data.model.BodyWithPosition

@Composable
expect fun SpaceScreen(
    planets: List<BodyWithPosition>,
    modifier: Modifier = Modifier
)