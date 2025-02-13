package com.nailorsh.astralis.core.di

import com.danielgergely.kgl.Kgl

internal actual fun createAppComponent(
    platformContext: PlatformContext,
    kgl: Kgl
): AppComponent {
    return AppComponent::class.create(platformContext, kgl)
}