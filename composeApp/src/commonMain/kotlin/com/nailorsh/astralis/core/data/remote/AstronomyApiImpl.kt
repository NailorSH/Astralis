package com.nailorsh.astralis.core.data.remote

import com.nailorsh.astralis.core.data.remote.model.AstronomicalBodyDto
import io.ktor.client.HttpClient
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@Suppress("RUNTIME_ANNOTATION_NOT_SUPPORTED")
@ContributesBinding(AppScope::class)
@Inject
@SingleIn(AppScope::class)
class AstronomyApiImpl(
    private val client: HttpClient
) : AstronomyApi {
    // https://docs.astronomyapi.com/endpoints/bodies/positions#get-all-bodies-positions
    override suspend fun getAllBodiesPositions(): List<AstronomicalBodyDto> = TODO()
}