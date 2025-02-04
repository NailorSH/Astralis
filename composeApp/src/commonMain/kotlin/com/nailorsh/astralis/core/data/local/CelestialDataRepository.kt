package com.nailorsh.astralis.core.data.local

import com.nailorsh.astralis.core.data.local.model.PlanetData

interface CelestialDataRepository {
    suspend fun getPlanets(): Map<String, PlanetData>
}
