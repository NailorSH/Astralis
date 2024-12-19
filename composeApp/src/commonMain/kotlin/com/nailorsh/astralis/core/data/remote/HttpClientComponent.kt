package com.nailorsh.astralis.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.DefaultJson
import io.ktor.serialization.kotlinx.json.json
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val BASE_URL = "https://api.astronomyapi.com/api/v2/"
private const val APPLICATION_ID = "209ce81a-6acc-4c6c-b6c7-097d6ef62a28"
private const val APPLICATION_SECRET = "df6f58bebdfd4e9f5a9bbf1362ea050bca6dc86773622e1ff995a8e477477d6a6d41a536e6201502a9b5edefc6d1e36cd3304e0971cc7c6285a15d329cfee819a8fa790d4ee558f66080682527261eb78ba6c690ddbad7d056e9e8c786e8b4048239086cdf0692c03e05feb647b2b416"

@ContributesTo(AppScope::class)
interface HttpClientComponent {
    @Provides
    fun provideHttpClient(): HttpClient = getHttpClient()
}

@OptIn(ExperimentalEncodingApi::class)
private fun getHttpClient(): HttpClient {
    val authString = "$APPLICATION_ID:$APPLICATION_SECRET"
    val encodedAuth = Base64.encode(authString.encodeToByteArray())

    return HttpClient(httpEngine()) {
        install(ContentNegotiation) {
            json(DefaultJson)
        }

        defaultRequest {
            url(BASE_URL)
            header(HttpHeaders.Authorization, "Basic $encodedAuth")
        }
    }
}

expect fun httpEngine(): HttpClientEngine