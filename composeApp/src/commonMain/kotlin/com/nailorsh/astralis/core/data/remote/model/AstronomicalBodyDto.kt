package com.nailorsh.astralis.core.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class AstronomicalBodyDto(
    val id: String,
    val name: String,
    val distance: DistanceDto,
    val position: PositionDto,
    val extraInfo: ExtraInfoDto
)