package com.nailorsh.astralis.core.arch

import androidx.annotation.CallSuper
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.nailorsh.astralis.core.coroutines.AppDispatchers
import com.nailorsh.astralis.core.lifecycle.DecomposeViewModelCoroutineScopeProvider
import kotlinx.coroutines.SupervisorJob

abstract class DecomposeViewModel : InstanceKeeper.Instance, LifecycleOwner {
    private val registry by lazy { LifecycleRegistry(Lifecycle.State.INITIALIZED) }
    override val lifecycle = registry

    protected val viewModelScope = DecomposeViewModelCoroutineScopeProvider.provideCoroutineScope(
        lifecycleOwner = this@DecomposeViewModel,
        context = SupervisorJob() + AppDispatchers.WorkStealing
    )

    init {
        registry.onCreate()
        registry.onStart()
        registry.onResume()
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        registry.onPause()
        registry.onStop()
        registry.onDestroy()
    }
}