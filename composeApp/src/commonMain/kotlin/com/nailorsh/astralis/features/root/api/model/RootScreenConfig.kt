package com.nailorsh.astralis.features.root.api.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface RootScreenConfig {
    @Serializable
    data object Home : RootScreenConfig
}