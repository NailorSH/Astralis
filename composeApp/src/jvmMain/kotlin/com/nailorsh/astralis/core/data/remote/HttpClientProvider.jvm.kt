package com.nailorsh.astralis.core.data.remote

import io.ktor.client.engine.cio.CIO

actual fun httpEngine() = CIO.create()