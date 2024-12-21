package com.nailorsh.astralis.core.data.remote.model.response

import com.nailorsh.astralis.core.data.remote.model.BodyDto
import com.nailorsh.astralis.core.data.remote.model.BodyPositionDto
import kotlinx.serialization.Serializable

@Serializable
data class BodyPositionsRowDto(
    val body: BodyDto,
    val positions: List<BodyPositionDto>
)