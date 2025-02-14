package com.nailorsh.astralis.features.home.impl.presentation.viewmodel

import com.nailorsh.astralis.core.arch.State
import com.nailorsh.astralis.features.home.impl.presentation.data.model.BodyWithPosition

data class HomeState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,

    val planets: List<BodyWithPosition> = emptyList(),
    var isCameraOn: Boolean = false,

    val showLoadingScreen: Boolean = false
) : State
