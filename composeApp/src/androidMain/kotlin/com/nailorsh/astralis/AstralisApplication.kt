package com.nailorsh.astralis

import android.app.Application

class AstralisApplication @JvmOverloads constructor(
    private val platform: AstralisAppPlatform = AstralisAppPlatform()
) : Application() {
    override fun onCreate() {
        super.onCreate()
        platform.start(
            platformContext = this,
        )
    }
}