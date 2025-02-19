package com.nailorsh.astralis.core.data.remote

import io.ktor.client.engine.js.Js

actual fun httpEngine() = Js.create()