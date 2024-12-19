package com.nailorsh.astralis.core.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class EquatorialPositionDto(
    val rightAscension: DegreeStringDto,
    val declination: DegreeStringDto
)