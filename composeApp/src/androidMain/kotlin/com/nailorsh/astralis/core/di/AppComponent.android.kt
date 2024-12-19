package com.nailorsh.astralis.core.di

internal actual fun createAppComponent(
    platformContext: PlatformContext
): AppComponent {
    return AppComponent::class.create(platformContext)
}