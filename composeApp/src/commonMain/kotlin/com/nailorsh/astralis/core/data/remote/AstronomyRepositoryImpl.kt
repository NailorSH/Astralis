package com.nailorsh.astralis.core.data.remote

import io.ktor.client.HttpClient

class AstronomyRepositoryImpl(
    private val client: HttpClient
) : AstronomyRepository {
}