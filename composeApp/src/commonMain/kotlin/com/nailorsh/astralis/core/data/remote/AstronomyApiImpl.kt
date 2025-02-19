package com.nailorsh.astralis.core.data.remote

import com.nailorsh.astralis.core.data.remote.model.BodyPositionDto
import com.nailorsh.astralis.core.data.remote.model.response.BodiesDto
import com.nailorsh.astralis.core.data.remote.model.response.BodyPositionsDto
import com.nailorsh.astralis.core.data.remote.model.response.ResponseDto
import com.nailorsh.astralis.core.utils.format.HOUR_MINUTE_SECOND
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
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
    override suspend fun getBodies(): Result<List<String>> = runCatching {
        val request = client.get("bodies")
        request.body<ResponseDto<BodiesDto>>().data.bodies
    }

    // https://docs.astronomyapi.com/endpoints/bodies/positions#get-all-bodies-positions
    override suspend fun getAllBodiesPositions(
        latitude: Double,
        longitude: Double,
        elevation: Double,
        fromDate: LocalDate,
        toDate: LocalDate,
        time: LocalTime
    ): Result<List<BodyPositionDto>> = runCatching {
        val request = client.get("bodies/positions") {
            parameter("latitude", latitude)
            parameter("longitude", longitude)
            parameter("elevation", elevation)
            parameter("from_date", fromDate)
            parameter("to_date", toDate)
            parameter("time", time.format(LocalTime.Formats.HOUR_MINUTE_SECOND))
            parameter("output", "rows")
        }
        request.body<ResponseDto<BodyPositionsDto>>().data.rows.map { it.positions.first() }
    }
}