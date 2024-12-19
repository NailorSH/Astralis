package com.nailorsh.astralis.core.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class HorizontalPositionDto(
    val altitude: DegreeStringDto,
    val azimuth: DegreeStringDto
)