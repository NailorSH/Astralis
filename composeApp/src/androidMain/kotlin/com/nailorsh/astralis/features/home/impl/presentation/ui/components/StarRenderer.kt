package com.nailorsh.astralis.features.home.impl.presentation.ui.components

import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class StarData(
    val x: Float, // Позиция по X
    val y: Float, // Позиция по Y
    val z: Float, // Позиция по Z
    val brightness: Float // Яркость звезды (от 0 до 1)
)

class StarRenderer(
    private val program: Int
) {
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var indexBuffer: ShortBuffer
    private var indexCount: Int = 0

    init {
        // Инициализация буферов для сферы
        generateSphereData(10, 10)
    }

    fun renderStars(stars: List<StarData>, viewMatrix: FloatArray, projectionMatrix: FloatArray) {
        stars.forEach { star ->
            renderStar(star, viewMatrix, projectionMatrix)
        }
    }

    private fun renderStar(star: StarData, viewMatrix: FloatArray, projectionMatrix: FloatArray) {
        GLES20.glUseProgram(program)

        // Устанавливаем позицию звезды
        val modelMatrix = FloatArray(16)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, star.x, star.y, star.z)

        // Устанавливаем масштаб в зависимости от яркости
        val scale = star.brightness * 0.1f // Масштабируем яркость
        Matrix.scaleM(modelMatrix, 0, scale, scale, scale)

        // Умножаем на матрицу вида и проекции
        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        // Передаем матрицу в шейдер
        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        // Устанавливаем цвет звезды (например, белый)
        val colorHandle = GLES20.glGetUniformLocation(program, "uColor")
        GLES20.glUniform4f(colorHandle, 1f, 1f, 1f, 1f) // Белый цвет

        // Рисуем звезду
        val positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            indexCount,
            GLES20.GL_UNSIGNED_SHORT,
            indexBuffer
        )

        GLES20.glDisableVertexAttribArray(positionHandle)
    }

    private fun generateSphereData(latitudeBands: Int, longitudeBands: Int, radius: Double = 1.0) {
        val vertices = mutableListOf<Float>()
        val indices = mutableListOf<Short>()

        // Генерация вершин
        for (lat in 0..latitudeBands) {
            val theta = lat * Math.PI / latitudeBands
            val sinTheta = sin(theta)
            val cosTheta = cos(theta)

            for (lon in 0..longitudeBands) {
                val phi = lon * 2 * Math.PI / longitudeBands
                val sinPhi = sin(phi)
                val cosPhi = cos(phi)

                val x = cosPhi * sinTheta * radius
                val y = cosTheta * radius
                val z = sinPhi * sinTheta * radius

                vertices.add(x.toFloat())
                vertices.add(y.toFloat())
                vertices.add(z.toFloat())
            }
        }

        // Генерация индексов
        for (lat in 0 until latitudeBands) {
            for (lon in 0 until longitudeBands) {
                val first = (lat * (longitudeBands + 1) + lon)
                val second = first + longitudeBands + 1

                indices.add(first.toShort())
                indices.add((first + 1).toShort())
                indices.add(second.toShort())

                indices.add(second.toShort())
                indices.add((first + 1).toShort())
                indices.add((second + 1).toShort())
            }
        }

        indexCount = indices.size

        // Создание буфера вершин
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertices.toFloatArray())
                position(0)
            }
        }

        // Создание буфера индексов
        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(indices.toShortArray())
                position(0)
            }
        }
    }
}

fun generateRandomStars(count: Int, radius: Float = 10f): List<StarData> {
    val stars = mutableListOf<StarData>()
    val random = Random(System.currentTimeMillis()) // Инициализация генератора случайных чисел

    for (i in 0 until count) {
        // Генерация случайных углов
        val theta = random.nextDouble() * PI // Зенит (от 0 до π)
        val phi = random.nextDouble() * 2 * PI // Азимут (от 0 до 2π)

        // Преобразование в декартовы координаты
        val x = radius * sin(theta) * cos(phi)
        val y = radius * sin(theta) * sin(phi)
        val z = radius * cos(theta)

        // Генерация случайной яркости в диапазоне [0.5, 1.0]
        val brightness = random.nextFloat() * 0.5f + 0.5f

        stars.add(StarData(x.toFloat(), y.toFloat(), z.toFloat(), brightness))
    }

    return stars
}