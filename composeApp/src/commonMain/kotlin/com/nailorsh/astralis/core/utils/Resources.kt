package com.nailorsh.astralis.core.utils

import androidx.compose.ui.graphics.ImageBitmap
import astralis.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap

@OptIn(ExperimentalResourceApi::class)
suspend fun getImageBitmap(path: String): ImageBitmap {
    val bytes = Res.readBytes(path)
    return bytes.decodeToImageBitmap()
}