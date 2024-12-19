package com.nailorsh.astralis.core.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class DegreeStringDto(
    val degrees: String,
    val string: String
)