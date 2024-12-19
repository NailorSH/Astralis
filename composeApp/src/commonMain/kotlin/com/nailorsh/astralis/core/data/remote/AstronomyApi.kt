package com.nailorsh.astralis.core.data.remote

import com.nailorsh.astralis.core.data.remote.model.AstronomicalBodyDto

interface AstronomyApi {
    suspend fun getAllBodiesPositions(): List<AstronomicalBodyDto>
}