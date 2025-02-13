package com.nailorsh.astralis.core.di

import com.danielgergely.kgl.Kgl
import com.nailorsh.astralis.core.data.local.CelestialDataRepository
import com.nailorsh.astralis.features.root.api.RootDecomposeComponent
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn
import kotlin.properties.Delegates.notNull

internal expect fun createAppComponent(
    platformContext: PlatformContext,
    kgl: Kgl
): AppComponent

@Suppress("RUNTIME_ANNOTATION_NOT_SUPPORTED")
@MergeComponent(AppScope::class)
@SingleIn(AppScope::class)
abstract class AppComponent(
    @get:[Provides SingleIn(AppScope::class)]
    protected val platformContext: PlatformContext,
    @get:[Provides SingleIn(AppScope::class)]
    protected val kgl: Kgl
) {
    abstract val celestialDataRepository: CelestialDataRepository
    abstract val rootDecomposeComponentFactory: RootDecomposeComponent.Factory

    companion object {
        var appComponent: AppComponent by notNull()
            private set

        internal fun create(
            platformContext: PlatformContext,
            kgl: Kgl
        ): AppComponent {
            appComponent = createAppComponent(platformContext, kgl)
            return appComponent
        }
    }
}