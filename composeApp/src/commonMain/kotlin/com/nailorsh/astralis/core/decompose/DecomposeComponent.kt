package com.nailorsh.astralis.core.decompose

import androidx.compose.runtime.Composable

abstract class DecomposeComponent internal constructor() {
    @Composable
    abstract fun Render()
}