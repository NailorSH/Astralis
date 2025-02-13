package com.nailorsh.astralis.core.arch

import kotlinx.coroutines.flow.StateFlow

abstract class BaseViewModel<A : Action, S : State> : DecomposeViewModel() {
    abstract val state: StateFlow<S>

    abstract fun onAction(action: A)
}