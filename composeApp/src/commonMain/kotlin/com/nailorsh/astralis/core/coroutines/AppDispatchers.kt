package com.nailorsh.astralis.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher

expect val ioDispatcher: CoroutineDispatcher
expect val workStealingDispatcher: CoroutineDispatcher

object AppDispatchers {
    val Default: CoroutineDispatcher = Dispatchers.Default
    val IO: CoroutineDispatcher = ioDispatcher
    val Main: MainCoroutineDispatcher = Dispatchers.Main
    val Unconfined: CoroutineDispatcher = Dispatchers.Unconfined
    val WorkStealing: CoroutineDispatcher = workStealingDispatcher
}