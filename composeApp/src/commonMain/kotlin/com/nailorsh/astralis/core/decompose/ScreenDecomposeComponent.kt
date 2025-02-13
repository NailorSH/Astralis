package com.nailorsh.astralis.core.decompose

import com.arkivanov.decompose.ComponentContext

abstract class ScreenDecomposeComponent(
    componentContext: ComponentContext
) : DecomposeComponent(), ComponentContext by componentContext