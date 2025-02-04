package com.nailorsh.astralis.core.data.local

import astralis.composeapp.generated.resources.Res
import com.nailorsh.astralis.core.data.local.model.PlanetData
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.ExperimentalResourceApi
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

const val PLANETS_DATA_PATH = "files/ssystem_major.json"

@Suppress("RUNTIME_ANNOTATION_NOT_SUPPORTED")
@ContributesBinding(AppScope::class)
@Inject
@SingleIn(AppScope::class)
class CelestialDataRepositoryImpl : CelestialDataRepository {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun getPlanets(): Map<String, PlanetData> {
        val bytes = Res.readBytes(PLANETS_DATA_PATH)
        val jsonString = bytes.decodeToString()

        return json.decodeFromString(jsonString)
    }
}