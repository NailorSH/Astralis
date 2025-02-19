package com.nailorsh.astralis.core.data.remote

import com.nailorsh.astralis.core.data.remote.model.BodyPositionDto
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

interface AstronomyApi {
    suspend fun getBodies(): Result<List<String>>
    suspend fun getAllBodiesPositions(
        latitude: Double,
        longitude: Double,
        elevation: Double,
        fromDate: LocalDate,
        toDate: LocalDate,
        time: LocalTime
    ): Result<List<BodyPositionDto>>
}