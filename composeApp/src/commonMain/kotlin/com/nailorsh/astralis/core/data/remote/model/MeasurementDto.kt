package com.nailorsh.astralis.core.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class MeasurementDto(
    val au: String, // астрономическая единица
    val km: String // расстояние в километрах
)