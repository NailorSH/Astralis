package com.nailorsh.astralis.features.home.impl.presentation.ui.components

import androidx.compose.ui.graphics.Matrix
import com.danielgergely.kgl.FloatBuffer
import com.danielgergely.kgl.GL_BLEND
import com.danielgergely.kgl.GL_FLOAT
import com.danielgergely.kgl.GL_LINES
import com.danielgergely.kgl.GL_ONE_MINUS_SRC_ALPHA
import com.danielgergely.kgl.GL_SRC_ALPHA
import com.danielgergely.kgl.GL_UNSIGNED_SHORT
import com.danielgergely.kgl.IntBuffer
import com.danielgergely.kgl.Kgl
import com.danielgergely.kgl.Program
import com.nailorsh.astralis.core.kgl.drawElements
import com.nailorsh.astralis.core.kgl.vertexAttribPointer
import com.nailorsh.astralis.core.utils.graphics.math.multiplyMM
import com.nailorsh.astralis.core.utils.graphics.math.setIdentityM
import com.nailorsh.astralis.core.utils.graphics.math.translateM

class GridRenderer(
    private val programSkySphere: Program,
    private val vertexBuffer: FloatBuffer?,
    private val wireframeIndexBuffer: IntBuffer?,
    private val kgl: Kgl
) {
    fun drawGridLines(
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray,
        eyeX: Float, eyeY: Float, eyeZ: Float
    ) {
        kgl.useProgram(programSkySphere)

        val mvpMatrixHandle = kgl.getUniformLocation(programSkySphere, "uMVPMatrix")

        // Двигаем сферу в позицию камеры
        val gridLinesSphereModelMatrix = FloatArray(16)
        Matrix.setIdentityM(gridLinesSphereModelMatrix, 0)
        Matrix.translateM(gridLinesSphereModelMatrix, 0, eyeX, eyeY, eyeZ) // Двигаем к камере

        // Создаём итоговую матрицу
        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, gridLinesSphereModelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        mvpMatrixHandle?.let { kgl.uniformMatrix4fv(it, false, mvpMatrix) }

        val positionHandle = kgl.getAttribLocation(programSkySphere, "aPosition")
        kgl.enableVertexAttribArray(positionHandle)
        vertexBuffer?.let { kgl.vertexAttribPointer(positionHandle, 3, GL_FLOAT, false, 20, it) }

        kgl.enable(GL_BLEND)
        kgl.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        val colorHandle = kgl.getUniformLocation(programSkySphere, "uColor")
        colorHandle?.let { kgl.uniform4f(it, 0f, 0f, 0f, 1f) }

        wireframeIndexBuffer?.let {
            kgl.drawElements(
                GL_LINES,
                6,
                GL_UNSIGNED_SHORT,
                it
            )
        }

        kgl.disableVertexAttribArray(positionHandle)
    }
}