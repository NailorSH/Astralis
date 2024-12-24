package com.nailorsh.astralis.core.utils.graphics

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils

fun loadTexture(context: Context, resourceId: Int): Int {
    val textureHandle = IntArray(1)
    GLES20.glGenTextures(1, textureHandle, 0)

    if (textureHandle[0] != 0) {
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])

        // Устанавливаем параметры текстуры
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )

        // Загружаем изображение в текстуру
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle() // Освобождаем ресурсы

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0) // Отвязываем текстуру
    }

    return textureHandle[0]
}