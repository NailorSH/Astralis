package com.nailorsh.astralis.features.home.impl.presentation.ui.components

import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class GridRenderer(
    private val programSkySphere: Int,
    private val vertexBuffer: FloatBuffer?,
    private val wireframeIndexBuffer: ShortBuffer?
) {

    fun drawGridLines(
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray,
        eyeX: Float,
        eyeY: Float,
        eyeZ: Float
    ) {
        GLES20.glUseProgram(programSkySphere)

        val mvpMatrixHandle = GLES20.glGetUniformLocation(programSkySphere, "uMVPMatrix")

        // Двигаем сферу в позицию камеры
        val gridLinesSphereModelMatrix = FloatArray(16)
        Matrix.setIdentityM(gridLinesSphereModelMatrix, 0)
        Matrix.translateM(gridLinesSphereModelMatrix, 0, eyeX, eyeY, eyeZ) // Двигаем к камере

        // Создаём итоговую матрицу
        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, gridLinesSphereModelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        val positionHandle = GLES20.glGetAttribLocation(programSkySphere, "aPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 5 * 4, vertexBuffer)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        val colorHandle = GLES20.glGetUniformLocation(programSkySphere, "uColor")
        GLES20.glUniform4f(colorHandle, 0f, 0f, 0f, 1f) // Чёрный цвет, полностью непрозрачный

        GLES20.glDrawElements(
            GLES20.GL_LINES,
            wireframeIndexBuffer!!.limit(),
            GLES20.GL_UNSIGNED_SHORT,
            wireframeIndexBuffer
        )

        GLES20.glDisableVertexAttribArray(positionHandle)
    }
}