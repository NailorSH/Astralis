package com.nailorsh.astralis.core.data.remote.model.response

import kotlinx.serialization.Serializable

@Serializable
data class BodiesDto(
    val bodies: List<String>
)