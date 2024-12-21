package com.nailorsh.astralis.core.data.remote.model.response

import com.nailorsh.astralis.core.data.remote.model.FromToDatesDto
import com.nailorsh.astralis.core.data.remote.model.ObserverDto
import kotlinx.serialization.Serializable

@Serializable
data class BodyPositionsDto(
    val dates: FromToDatesDto,
    val observer: ObserverDto,
    val rows: List<BodyPositionsRowDto>
)