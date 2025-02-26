package com.nailorsh.astralis.features.home.impl.presentation.ui.components

import android.opengl.GLES20
import android.opengl.Matrix
import com.nailorsh.astralis.features.home.impl.presentation.data.model.BodyWithPosition
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.cos
import kotlin.math.sin

class PlanetRenderer(
    private val programPlanets: Int,
    private val vertexBuffer: FloatBuffer?,
    private val indexBuffer: ShortBuffer?,
    private val indexCount: Int
) {

    fun renderPlanets(
        planets: List<BodyWithPosition>,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray
    ) {
        planets.forEach { planet ->
            // Конвертация горизонтальных координат в декартовы
            val altitudeRadians = Math.toRadians(planet.altitudeDegrees)
            val azimuthRadians = Math.toRadians(planet.azimuthDegrees)

            val distanceMultiplier = 50.0 // Умножаем на 50 для увеличения расстояния
            val x = cos(altitudeRadians) * sin(azimuthRadians) * distanceMultiplier
            val y = sin(altitudeRadians) * distanceMultiplier
            val z = cos(altitudeRadians) * cos(azimuthRadians) * distanceMultiplier

            // Устанавливаем позицию планеты в пространстве
            val position = floatArrayOf(x.toFloat(), y.toFloat(), z.toFloat())

            // Определяем масштаб (по расстоянию до Земли)
            val scale = (1.0 / planet.distanceFromEarthAU).toFloat()

            // Модельная матрица для планеты
            val modelMatrix = FloatArray(16).apply {
                Matrix.setIdentityM(this, 0)
                Matrix.translateM(this, 0, position[0], position[1], position[2])
                Matrix.scaleM(this, 0, scale, scale, scale)
            }

            // Итоговая MVP-матрица
            val mvpMatrix = FloatArray(16).apply {
                Matrix.multiplyMM(this, 0, viewMatrix, 0, modelMatrix, 0)
                Matrix.multiplyMM(this, 0, projectionMatrix, 0, this, 0)
            }

            // Рисуем планету
            drawPlanet(mvpMatrix, planet)
        }
    }

    private fun drawPlanet(mvpMatrix: FloatArray, planet: BodyWithPosition) {
        GLES20.glUseProgram(programPlanets)

        val mvpMatrixHandle = GLES20.glGetUniformLocation(programPlanets, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        val positionHandle = GLES20.glGetAttribLocation(programPlanets, "aPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 5 * 4, vertexBuffer)

        val texCoordHandle = GLES20.glGetAttribLocation(programPlanets, "aTexCoord")
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        val texCoordBuffer = vertexBuffer!!.duplicate()
        texCoordBuffer.position(3)
        GLES20.glVertexAttribPointer(
            texCoordHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            5 * 4,
            texCoordBuffer
        )

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, planet.texture.id ?: 0)

        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            indexCount,
            GLES20.GL_UNSIGNED_SHORT,
            indexBuffer
        )

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }
}