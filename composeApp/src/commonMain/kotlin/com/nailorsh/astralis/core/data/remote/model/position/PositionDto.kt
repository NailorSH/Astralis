package com.nailorsh.astralis.core.data.remote.model.position

import kotlinx.serialization.Serializable

@Serializable
data class PositionDto(
    val horizontal: HorizontalPositionDto,
    val equatorial: EquatorialPositionDto,
    val constellation: ConstellationDto
)
