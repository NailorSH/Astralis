package com.nailorsh.astralis.features.root.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext

interface RootDecomposeComponent : ComponentContext,
    RootNavigationInterface {
    @Composable
    fun Render(modifier: Modifier)

    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
        ): RootDecomposeComponent
    }
}