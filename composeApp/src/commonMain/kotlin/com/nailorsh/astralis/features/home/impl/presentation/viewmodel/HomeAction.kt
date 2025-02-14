package com.nailorsh.astralis.features.home.impl.presentation.viewmodel

import com.nailorsh.astralis.core.arch.Action

sealed interface HomeAction : Action {
    object OnCameraClicked : HomeAction
}