package com.nailorsh.astralis.core.data.remote

import io.ktor.client.engine.okhttp.OkHttp

actual fun httpEngine() = OkHttp.create()