package com.nailorsh.astralis.core.data.remote.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class FromToDatesDto(
    val from: Instant,
    val to: Instant,
)