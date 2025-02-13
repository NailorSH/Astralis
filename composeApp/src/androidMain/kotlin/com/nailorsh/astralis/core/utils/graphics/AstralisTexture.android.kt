package com.nailorsh.astralis.core.utils.graphics

import android.opengl.GLUtils
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import com.danielgergely.kgl.Kgl

actual fun Kgl.texImage2D(target: Int, level: Int, bitmap: ImageBitmap, border: Int) {
    val androidBitmap = bitmap.asAndroidBitmap()
    GLUtils.texImage2D(target, level, androidBitmap, border)
    androidBitmap.recycle()
}