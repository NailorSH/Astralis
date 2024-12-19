package com.nailorsh.astralis.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
actual val workStealingDispatcher: CoroutineDispatcher = Dispatchers.Default