package com.nailorsh.astralis.core.data.local

import com.nailorsh.astralis.core.data.local.model.PlanetDataDto

interface CelestialDataRepository {
    suspend fun getPlanets(): Map<String, PlanetDataDto>
}
