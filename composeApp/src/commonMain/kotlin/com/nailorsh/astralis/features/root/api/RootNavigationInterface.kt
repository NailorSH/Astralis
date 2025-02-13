package com.nailorsh.astralis.features.root.api

import androidx.compose.runtime.staticCompositionLocalOf
import com.nailorsh.astralis.features.root.api.model.RootScreenConfig

val LocalRootNavigation =
    staticCompositionLocalOf<RootNavigationInterface> {
        error("CompositionLocal LocalRootComponent not present")
    }

interface RootNavigationInterface {
    fun push(config: RootScreenConfig)
}