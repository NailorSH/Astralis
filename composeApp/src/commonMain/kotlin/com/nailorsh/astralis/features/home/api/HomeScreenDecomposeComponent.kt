package com.nailorsh.astralis.features.home.api

import com.arkivanov.decompose.ComponentContext
import com.nailorsh.astralis.core.decompose.DecomposeOnBackParameter
import com.nailorsh.astralis.core.decompose.ScreenDecomposeComponent

abstract class HomeScreenDecomposeComponent(
    componentContext: ComponentContext
) : ScreenDecomposeComponent(componentContext) {
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onBackParameter: DecomposeOnBackParameter
        ): HomeScreenDecomposeComponent
    }
}