package com.nailorsh.astralis.core.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class AstronomyData(
    val date: String,
    val explanation: String,
    val url: String,
)

// Application Id: 209ce81a-6acc-4c6c-b6c7-097d6ef62a28
// Application Secret: df6f58bebdfd4e9f5a9bbf1362ea050bca6dc86773622e1ff995a8e477477d6a6d41a536e6201502a9b5edefc6d1e36cd3304e0971cc7c6285a15d329cfee819a8fa790d4ee558f66080682527261eb78ba6c690ddbad7d056e9e8c786e8b4048239086cdf0692c03e05feb647b2b416