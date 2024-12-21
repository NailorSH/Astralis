package com.nailorsh.astralis.core.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ObserverDto(
    val location: LocationDto
)