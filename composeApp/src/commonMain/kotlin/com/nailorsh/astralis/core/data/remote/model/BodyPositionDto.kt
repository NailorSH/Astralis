package com.nailorsh.astralis.core.data.remote.model

import com.nailorsh.astralis.core.data.remote.model.position.PositionDto
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class BodyPositionDto(
    val date: Instant,
    val id: String,
    val name: String,
    val distance: DistanceDto,
    val position: PositionDto,
    val extraInfo: ExtraInfoDto
)