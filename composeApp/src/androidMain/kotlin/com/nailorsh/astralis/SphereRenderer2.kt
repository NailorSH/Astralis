package com.nailorsh.astralis


import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import co.touchlab.kermit.Logger
import com.nailorsh.astralis.core.utils.graphics.compileShader
import com.nailorsh.astralis.core.utils.graphics.createProgram
import com.nailorsh.astralis.core.utils.graphics.loadTexture
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

val vertexShader = """
    attribute vec4 aPosition; // Позиция вершины
    attribute vec2 aTexCoord; // Текстурные координаты
    uniform mat4 uMVPMatrix; // Модельная проекционная матрица
    varying vec2 vTexCoord; // Передача текстурных координат во фрагментный шейдер

    void main() {
        gl_Position = uMVPMatrix * aPosition; // Множим позицию вершины на модельную матрицу
        vTexCoord = aTexCoord; // Передаем текстурные координаты
    }
""".trimIndent()

val fragmentShader = """
    precision mediump float;
    uniform sampler2D uTexture; // Текстура
    varying vec2 vTexCoord; // Текстурные координаты

    void main() {
        gl_FragColor = texture2D(uTexture, vTexCoord); // Получаем цвет из текстуры по текстурным координатам
    }
""".trimIndent()

private val fragmentShaderNoTexture = """
    precision mediump float;
    uniform vec4 uColor;

    void main() {
        gl_FragColor = uColor;
    }
""".trimIndent()

class SphereRenderer2(private val context: Context) : GLSurfaceView.Renderer {
    private var useTexture = true
    private var wireframeMode = false // Флаг для режима wireframe

    // Матрицы
    private val modelMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    // OpenGL ресурсы
    private var program = 0
    private var textureId = 0

    // Буферы для данных сферы
    private var vertexBuffer: FloatBuffer? = null
    private var indexBuffer: ShortBuffer? = null
    private var wireframeIndexBuffer: ShortBuffer? = null
    private var indexCount = 0

    // Параметры анимации
    private var alpha = 45f
    private var beta = 45f
    private var size = 0.3f
    private var position = floatArrayOf(0f, 0f, 0f)
    private var velocity = floatArrayOf(0.1f, 0.01f, 0.2f)
    private val boundaries = floatArrayOf(1f, 1f, 1f)
    private val sphereRadius = 0.2f
    private val timeStep = 0.016f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // Инициализация программы на основе флага useTexture
        updateShaderProgram()

        // Загрузка текстуры
        textureId = loadTexture(context, R.drawable.background)

        // Генерация данных сферы
        generateSphereData(40, 40)

        // Настройка фильтрации текстуры
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
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
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val aspectRatio = width.toFloat() / height
        Matrix.perspectiveM(projectionMatrix, 0, 45f, aspectRatio, 0.1f, 100f)

        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 0f, 0f,  // Камера находится в центре сферы
            0f, 0f, -1f, // Смотрит вперёд по оси -Z
            0f, 1f, 0f   // Верхняя часть направлена вдоль оси Y
        )
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        updatePosition()

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, position[0], position[1], position[2])
        Matrix.scaleM(modelMatrix, 0, size, size, size)
        Matrix.rotateM(modelMatrix, 0, alpha, 0f, 1f, 0f)
        Matrix.rotateM(modelMatrix, 0, beta, 1f, 0f, 0f)

        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        drawSphere()
    }

    private fun updateShaderProgram() {
        val vertexShaderCode = vertexShader
        val fragmentShaderCode = if (useTexture) fragmentShader else fragmentShaderNoTexture

        program = initializeShaderProgram(vertexShaderCode, fragmentShaderCode, useTexture)
    }

    private fun updatePosition() {
        alpha++
        for (i in position.indices) {
            position[i] += velocity[i] * timeStep
            if (kotlin.math.abs(position[i]) + sphereRadius >= boundaries[i]) {
                velocity[i] = -velocity[i]
                position[i] = kotlin.math.sign(position[i]) * (boundaries[i] - sphereRadius)
            }
        }
    }

    private fun drawSphere() {
        GLES20.glUseProgram(program)

        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        val positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 5 * 4, vertexBuffer)

        if (useTexture) {
            val texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
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

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        } else {
            val colorHandle = GLES20.glGetUniformLocation(program, "uColor")
            GLES20.glUniform4f(colorHandle, 1f, 0f, 0f, 1f) // Устанавливаем цвет (красный)
        }

        if (wireframeMode) {
            GLES20.glDrawElements(
                GLES20.GL_LINES,
                wireframeIndexBuffer!!.limit(),
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
        if (useTexture) {
            GLES20.glDisableVertexAttribArray(GLES20.glGetAttribLocation(program, "aTexCoord"))
        }
    }

    private fun generateSphereData(latitudeBands: Int, longitudeBands: Int) {
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
                val u = lon.toFloat() / longitudeBands // Текстурные координаты U
                val v = lat.toFloat() / latitudeBands // Текстурные координаты V

                Logger.d("TextureCoord") { "Vertex: ($x, $y, $z), TexCoord: ($u, $v)" }

                // Генерация вершин
                vertices.addAll(
                    listOf(
                        x.toFloat(), y.toFloat(), z.toFloat(),
                        u, v // Текстурные координаты
                    )
                )
            }
        }

        // Индексы для треугольников
        for (lat in 0 until latitudeBands) {
            for (lon in 0 until longitudeBands) {
                val first = (lat * (longitudeBands + 1) + lon).toShort()
                val second = (first + longitudeBands + 1).toShort()

//                indices.add(first)
//                indices.add(second)
//                indices.add((first + 1).toShort())
//
//                indices.add(second)
//                indices.add((second + 1).toShort())
//                indices.add((first + 1).toShort())

                // Для триангуляции
                indices.add(first)
                indices.add((first + 1).toShort())
                indices.add(second)

                indices.add(second)
                indices.add((first + 1).toShort())
                indices.add((second + 1).toShort())

                // Wireframe indices
                wireframeIndices.add(first)
                wireframeIndices.add(second)
                wireframeIndices.add(first)
                wireframeIndices.add((first + 1).toShort())
                wireframeIndices.add(second)
                wireframeIndices.add((second + 1).toShort())
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

        wireframeIndexBuffer = ByteBuffer.allocateDirect(wireframeIndices.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(wireframeIndices.toShortArray())
                position(0)
            }
        }
    }

    private fun initializeShaderProgram(
        vertexShaderCode: String,
        fragmentShaderCode: String,
        useTexture: Boolean
    ): Int {
        val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader =
            compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        val program = createProgram(vertexShader, fragmentShader)

        GLES20.glUseProgram(program)

        if (useTexture) {
            val textureHandle = GLES20.glGetUniformLocation(program, "uTexture")
            GLES20.glUniform1i(
                textureHandle,
                0
            ) // Указываем, что текстура будет использоваться в unit 0
        }

        return program
    }

    fun toggleWireframeMode() {
        wireframeMode = !wireframeMode
    }

    fun toggleTextureMode() {
        useTexture = !useTexture
    }
}
