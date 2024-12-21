package com.nailorsh.astralis.core.data.remote.model.response

import kotlinx.serialization.Serializable

@Serializable
data class ResponseDto<T>(
    val data: T
)

