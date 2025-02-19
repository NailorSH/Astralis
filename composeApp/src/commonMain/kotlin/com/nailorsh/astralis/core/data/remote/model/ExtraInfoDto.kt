package com.nailorsh.astralis.core.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ExtraInfoDto(
    val elongation: Double?,
    val magnitude: Double?
)