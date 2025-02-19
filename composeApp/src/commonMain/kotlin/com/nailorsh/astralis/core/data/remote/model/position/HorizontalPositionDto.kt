package com.nailorsh.astralis.core.data.remote.model.position

import com.nailorsh.astralis.core.data.remote.model.DegreesStringDto
import kotlinx.serialization.Serializable

@Serializable
data class HorizontalPositionDto(
    val altitude: DegreesStringDto,
    val azimuth: DegreesStringDto
)