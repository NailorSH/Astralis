package com.nailorsh.astralis.core.data.remote

import com.nailorsh.astralis.core.data.remote.model.AstronomicalBodyDto

interface AstronomyApi {
    suspend fun getBodies(): Result<List<AstronomicalBodyDto>>
    suspend fun getAllBodiesPositions(): List<AstronomicalBodyDto>
}