package com.nailorsh.astralis.features.home.impl.presentation.ui.components

import androidx.compose.ui.graphics.Matrix
import com.danielgergely.kgl.FloatBuffer
import com.danielgergely.kgl.GL_FLOAT
import com.danielgergely.kgl.GL_TEXTURE_2D
import com.danielgergely.kgl.GL_TRIANGLES
import com.danielgergely.kgl.GL_UNSIGNED_SHORT
import com.danielgergely.kgl.IntBuffer
import com.danielgergely.kgl.Kgl
import com.danielgergely.kgl.Program
import com.nailorsh.astralis.core.kgl.drawElements
import com.nailorsh.astralis.core.kgl.vertexAttribPointer
import com.nailorsh.astralis.core.utils.graphics.math.multiplyMM
import com.nailorsh.astralis.core.utils.graphics.math.scaleM
import com.nailorsh.astralis.core.utils.graphics.math.setIdentityM
import com.nailorsh.astralis.core.utils.graphics.math.toRadians
import com.nailorsh.astralis.core.utils.graphics.math.translateM
import com.nailorsh.astralis.features.home.impl.presentation.data.model.BodyWithPosition
import kotlin.math.cos
import kotlin.math.sin

class PlanetRenderer(
    private val programPlanets: Program,
    private val vertexBuffer: FloatBuffer?,
    private val indexBuffer: IntBuffer?,
    private val indexCount: Int,
    private val kgl: Kgl
) {

    fun renderPlanets(
        planets: List<BodyWithPosition>,
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray
    ) {
        planets.forEach { planet ->
            // Конвертация горизонтальных координат в декартовы
            val altitudeRadians = planet.altitudeDegrees.toRadians()
            val azimuthRadians = planet.azimuthDegrees.toRadians()

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
        kgl.useProgram(programPlanets)

        val mvpMatrixHandle = kgl.getUniformLocation(programPlanets, "uMVPMatrix")
        mvpMatrixHandle?.let { kgl.uniformMatrix4fv(it, false, mvpMatrix) }

        val positionHandle = kgl.getAttribLocation(programPlanets, "aPosition")
        kgl.enableVertexAttribArray(positionHandle)
        vertexBuffer?.let { kgl.vertexAttribPointer(positionHandle, 3, GL_FLOAT, false, 5 * 4, it) }

        val texCoordHandle = kgl.getAttribLocation(programPlanets, "aTexCoord")
        kgl.enableVertexAttribArray(texCoordHandle)
        val texCoordBuffer = vertexBuffer!!
        texCoordBuffer.position = 3
        kgl.vertexAttribPointer(
            texCoordHandle,
            2,
            GL_FLOAT,
            false,
            5 * 4,
            texCoordBuffer
        )

        kgl.bindTexture(GL_TEXTURE_2D, planet.texture.id)

        indexBuffer?.let { kgl.drawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_SHORT, it) }

        kgl.disableVertexAttribArray(positionHandle)
        kgl.disableVertexAttribArray(texCoordHandle)
    }
}