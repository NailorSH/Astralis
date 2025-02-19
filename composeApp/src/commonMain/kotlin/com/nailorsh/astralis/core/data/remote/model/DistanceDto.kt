package com.nailorsh.astralis.core.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class DistanceDto(
    val fromEarth: MeasurementDto
)