package com.nailorsh.astralis.core.di

import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn
import kotlin.properties.Delegates.notNull

internal expect fun createAppComponent(
    platformContext: PlatformContext
): AppComponent

@Suppress("RUNTIME_ANNOTATION_NOT_SUPPORTED")
@MergeComponent(AppScope::class)
@SingleIn(AppScope::class)
abstract class AppComponent(
    @get:[Provides SingleIn(AppScope::class)]
    protected val platformContext: PlatformContext
) {

    companion object {
        var appComponent: AppComponent by notNull()
            private set

        internal fun create(
            platformContext: PlatformContext
        ): AppComponent {
            appComponent = createAppComponent(platformContext)
            return appComponent
        }
    }
}