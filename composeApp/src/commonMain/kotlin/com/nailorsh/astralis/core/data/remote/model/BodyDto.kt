package com.nailorsh.astralis.core.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class BodyDto(
    val id: String,
    val name: String,
)