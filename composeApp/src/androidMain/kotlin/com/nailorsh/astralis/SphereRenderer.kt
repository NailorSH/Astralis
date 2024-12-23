package com.nailorsh.astralis

import android.opengl.GLES20
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.cos
import kotlin.math.sin

class SphereRenderer {
    private var program: Int = 0
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var indexBuffer: ShortBuffer
    private var vertexCount: Int = 0
    private var indexCount: Int = 0

    // Индексы для "скелетного" режима
    private lateinit var wireframeIndexBuffer: ShortBuffer
    private var wireframeIndexCount: Int = 0

    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        void main() {
            gl_Position = uMVPMatrix * vPosition;
        }
    """

    private val fragmentShaderCode = """
        precision mediump float;
        uniform vec4 vColor;
        void main() {
            gl_FragColor = vColor;
        }
    """

    fun initialize() {
        generateSphere()

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    private fun generateSphere(latitudeBands: Int = 30, longitudeBands: Int = 30) {
        val vertices = mutableListOf<Float>()
        val indices = mutableListOf<Short>()
        val wireframeIndices = mutableListOf<Short>()

        for (lat in 0..latitudeBands) {
            val theta = Math.PI * lat / latitudeBands
            val sinTheta = sin(theta)
            val cosTheta = cos(theta)

            for (lon in 0..longitudeBands) {
                val phi = 2 * Math.PI * lon / longitudeBands
                val sinPhi = sin(phi)
                val cosPhi = cos(phi)

                val x = cosPhi * sinTheta
                val y = cosTheta
                val z = sinPhi * sinTheta

                vertices.addAll(listOf(x.toFloat(), y.toFloat(), z.toFloat()))
            }
        }

        for (lat in 0 until latitudeBands) {
            for (lon in 0 until longitudeBands) {
                val first = (lat * (longitudeBands + 1) + lon).toShort()
                val second = (first + longitudeBands + 1).toShort()

                // Треугольные индексы
                indices.add(first)
                indices.add(second)
                indices.add((first + 1).toShort())

                indices.add(second)
                indices.add((second + 1).toShort())
                indices.add((first + 1).toShort())

                // Индексы линий для "скелетного" режима
                wireframeIndices.add(first)
                wireframeIndices.add(second)

                wireframeIndices.add(first)
                wireframeIndices.add((first + 1).toShort())

                wireframeIndices.add(second)
                wireframeIndices.add((second + 1).toShort())
            }
        }

        vertexCount = vertices.size / 3
        indexCount = indices.size
        wireframeIndexCount = wireframeIndices.size

        vertexBuffer = java.nio.ByteBuffer.allocateDirect(vertices.size * 4).run {
            order(java.nio.ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertices.toFloatArray())
                position(0)
            }
        }

        indexBuffer = java.nio.ByteBuffer.allocateDirect(indices.size * 2).run {
            order(java.nio.ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(indices.toShortArray())
                position(0)
            }
        }

        wireframeIndexBuffer = java.nio.ByteBuffer.allocateDirect(wireframeIndices.size * 2).run {
            order(java.nio.ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(wireframeIndices.toShortArray())
                position(0)
            }
        }
    }

    fun draw(mvpMatrix: FloatArray, color: FloatArray, wireframeMode: Boolean = true) {
        GLES20.glUseProgram(program)

        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition").also {
            GLES20.glEnableVertexAttribArray(it)
            GLES20.glVertexAttribPointer(it, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        }

        val colorHandle = GLES20.glGetUniformLocation(program, "vColor")
        GLES20.glUniform4fv(colorHandle, 1, color, 0) // Передаем цвет (RGBA)

        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        if (wireframeMode) {
            GLES20.glDrawElements(
                GLES20.GL_LINES,
                indexCount,
                GLES20.GL_UNSIGNED_SHORT,
                wireframeIndexBuffer
            )
        } else {
            GLES20.glDrawElements(
                GLES20.GL_TRIANGLES,
                indexCount,
                GLES20.GL_UNSIGNED_SHORT,
                indexBuffer
            )
        }

        GLES20.glDisableVertexAttribArray(positionHandle)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
}


