package com.nailorsh.astralis.core.data.remote.model.position

import com.nailorsh.astralis.core.data.remote.model.DegreesStringDto
import com.nailorsh.astralis.core.data.remote.model.HoursStringDto
import kotlinx.serialization.Serializable

@Serializable
data class EquatorialPositionDto(
    val rightAscension: HoursStringDto,
    val declination: DegreesStringDto
)
