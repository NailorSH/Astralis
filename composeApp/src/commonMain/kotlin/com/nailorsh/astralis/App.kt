package com.nailorsh.astralis

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.animation.LocalStackAnimationProvider
import com.nailorsh.astralis.core.ui.theme.AppTheme
import com.nailorsh.astralis.core.ui.utils.AstralisStackAnimationProvider
import com.nailorsh.astralis.features.root.api.LocalRootNavigation
import com.nailorsh.astralis.features.root.api.RootDecomposeComponent

@Composable
internal fun App(rootComponent: RootDecomposeComponent) = AppTheme {
    CompositionLocalProvider(
        LocalRootNavigation provides rootComponent,
        LocalStackAnimationProvider provides AstralisStackAnimationProvider
    ) {
        rootComponent.Render(
            Modifier.fillMaxSize()
        )
    }
}