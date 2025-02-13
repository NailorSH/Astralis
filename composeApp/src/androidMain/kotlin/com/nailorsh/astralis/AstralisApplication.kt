package com.nailorsh.astralis

import android.app.Application
import com.danielgergely.kgl.KglAndroid

class AstralisApplication @JvmOverloads constructor(
    private val platform: AstralisAppPlatform = AstralisAppPlatform()
) : Application() {
    override fun onCreate() {
        super.onCreate()
        platform.start(
            platformContext = this,
            kgl = KglAndroid
        )
    }
}