package com.nailorsh.astralis.core.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    val longitude: Double,
    val latitude: Double,
    val elevation: Double,
)