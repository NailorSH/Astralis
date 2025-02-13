package com.nailorsh.astralis

import com.danielgergely.kgl.Kgl
import com.nailorsh.astralis.core.di.AppComponent
import com.nailorsh.astralis.core.di.PlatformContext
import kotlin.properties.Delegates.notNull

class AstralisAppPlatform {
    var appComponent: AppComponent by notNull()
        private set

    fun start(
        platformContext: PlatformContext,
        kgl: Kgl
    ) {
        appComponent = AppComponent.create(platformContext, kgl)
    }
}