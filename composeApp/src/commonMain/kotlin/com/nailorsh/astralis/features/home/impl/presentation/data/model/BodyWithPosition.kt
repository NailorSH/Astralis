package com.nailorsh.astralis.features.home.impl.presentation.data.model

import com.nailorsh.astralis.core.utils.graphics.AstralisTexture

data class BodyWithPosition(
    val id: String,
    val azimuthDegrees: Double,
    val altitudeDegrees: Double,
    val distanceFromEarthAU: Double,
    val radiusKm: Double,
    val texture: AstralisTexture
)