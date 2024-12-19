package com.nailorsh.astralis.core.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ConstellationDto(
    val id: String,
    val short: String,
    val name: String
)