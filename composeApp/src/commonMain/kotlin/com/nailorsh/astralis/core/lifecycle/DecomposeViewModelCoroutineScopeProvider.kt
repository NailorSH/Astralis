package com.nailorsh.astralis.core.lifecycle

import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

object DecomposeViewModelCoroutineScopeProvider {
    fun provideCoroutineScope(
        lifecycleOwner: LifecycleOwner,
        context: CoroutineContext
    ) = lifecycleOwner.coroutineScope(context)
}