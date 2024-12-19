package com.nailorsh.astralis.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

actual val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
actual val workStealingDispatcher: CoroutineDispatcher by lazy {
    Executors.newWorkStealingPool().asCoroutineDispatcher()
}