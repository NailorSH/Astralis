package com.nailorsh.astralis.core.data.remote

import co.touchlab.kermit.Logger
import com.nailorsh.astralis.core.data.remote.model.AstronomicalBodyDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
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
    // https://api.astronomyapi.com/api/v2/bodies
    override suspend fun getBodies(): Result<List<AstronomicalBodyDto>> = runCatching {
        val request = client.get("bodies")
        val response = request.bodyAsText()
        Logger.d("getBodies") { "Response: $response" }

        request.body<List<AstronomicalBodyDto>>()
    }

    // https://docs.astronomyapi.com/endpoints/bodies/positions#get-all-bodies-positions
    override suspend fun getAllBodiesPositions(): List<AstronomicalBodyDto> = TODO()
}