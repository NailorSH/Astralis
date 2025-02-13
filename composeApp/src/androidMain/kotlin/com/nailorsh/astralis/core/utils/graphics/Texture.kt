package com.nailorsh.astralis.core.utils.graphics

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLUtils
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import com.danielgergely.kgl.GL_ALPHA
import com.danielgergely.kgl.GL_CLAMP_TO_EDGE
import com.danielgergely.kgl.GL_LINEAR
import com.danielgergely.kgl.GL_RGB
import com.danielgergely.kgl.GL_RGBA
import com.danielgergely.kgl.GL_TEXTURE_2D
import com.danielgergely.kgl.GL_TEXTURE_MAG_FILTER
import com.danielgergely.kgl.GL_TEXTURE_MIN_FILTER
import com.danielgergely.kgl.GL_TEXTURE_WRAP_S
import com.danielgergely.kgl.GL_TEXTURE_WRAP_T
import com.danielgergely.kgl.GL_UNSIGNED_BYTE
import com.danielgergely.kgl.GL_UNSIGNED_SHORT_5_6_5
import com.danielgergely.kgl.KglAndroid.bindTexture
import com.danielgergely.kgl.KglAndroid.createTexture
import com.danielgergely.kgl.KglAndroid.texParameteri

fun loadTexture(context: Context, resourceId: Int): Int {
    val textureHandle = createTexture()

    if (textureHandle != 0) {
        bindTexture(GL_TEXTURE_2D, textureHandle)

        texParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        texParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        texParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        texParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()

        bindTexture(GL_TEXTURE_2D, null)
    }

    return textureHandle
}

fun getInternalFormat(imageBitmap: ImageBitmap): Int {
    return when (imageBitmap.config) {
        ImageBitmapConfig.Alpha8 -> GL_ALPHA
        ImageBitmapConfig.Argb8888 -> GL_RGBA
        ImageBitmapConfig.Rgb565 -> GL_RGB
        else -> throw IllegalArgumentException("Unknown internalformat")
    }
}

fun getType(imageBitmap: ImageBitmap): Int {
    return when (imageBitmap.config) {
        ImageBitmapConfig.Rgb565 -> GL_UNSIGNED_SHORT_5_6_5
        ImageBitmapConfig.Alpha8, ImageBitmapConfig.Argb8888 -> GL_UNSIGNED_BYTE
        else -> GL_UNSIGNED_BYTE
    }
}