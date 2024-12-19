package com.nailorsh.astralis.core.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class PositionDto(
    val horizontal: HorizontalPositionDto,
    val equatorial: EquatorialPositionDto,
    val constellation: ConstellationDto
)
