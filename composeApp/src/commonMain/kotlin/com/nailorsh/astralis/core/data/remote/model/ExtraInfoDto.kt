package com.nailorsh.astralis.core.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ExtraInfoDto(
    val elongation: Int,
    val magnitude: Double
)