package com.nailorsh.astralis


import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.nailorsh.astralis.core.utils.graphics.compileShader
import com.nailorsh.astralis.core.utils.graphics.createProgram
import com.nailorsh.astralis.features.home.impl.presentation.data.model.BodyWithPosition
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

val vertexShaderSkySphere = """
    attribute vec4 aPosition; // Позиция вершины
    attribute vec2 aTexCoord; // Текстурные координаты
    uniform mat4 uMVPMatrix; // Модельная проекционная матрица
    varying vec2 vTexCoord; // Передача текстурных координат во фрагментный шейдер

    void main() {
        gl_Position = uMVPMatrix * aPosition; // Множим позицию вершины на модельную матрицу
        vTexCoord = aTexCoord; // Передаем текстурные координаты
    }
""".trimIndent()

private val fragmentShaderSkySphereNoTexture = """
    precision mediump float;
    uniform vec4 uColor;

    void main() {
        gl_FragColor = uColor;
    }
""".trimIndent()

val vertexShaderPlanets = """
    attribute vec4 aPosition; // Позиция вершины
    attribute vec2 aTexCoord; // Текстурные координаты
    uniform mat4 uMVPMatrix; // Модельно-видовая проекционная матрица
    varying vec2 vTexCoord; // Передача текстурных координат во фрагментный шейдер

    void main() {
        gl_Position = uMVPMatrix * aPosition; // Позиция вершины
        vTexCoord = aTexCoord; // Передача текстурных координат
    }

""".trimIndent()

val fragmentShaderPlanets = """
    precision mediump float;
    uniform sampler2D uTexture; // Текстура
    varying vec2 vTexCoord; // Текстурные координаты

    void main() {
        gl_FragColor = texture2D(uTexture, vTexCoord); // Получение цвета из текстуры
    }

""".trimIndent()

class SpaceRenderer(
    private val planets: List<BodyWithPosition>
) : GLSurfaceView.Renderer {
    private var wireframeMode = true // Флаг для режима wireframe

    private var azimuth = 0f // Влево-вправо
    private var altitude = 0f // Вверх-вниз
    private var distance = 5f // Начальная дистанция (приближение)

    // Переменные для хранения позиции камеры
    private var eyeX = 0f
    private var eyeY = 0f
    private var eyeZ = 0f

    private var previousX = 0f
    private var previousY = 0f

    private lateinit var scaleDetector: ScaleGestureDetector

    // Матрицы
    private val modelMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    // OpenGL ресурсы
    private var programSkySphere = 0
    private var programPlanets = 0

    // Буферы для данных сферы
    private var vertexBuffer: FloatBuffer? = null
    private var indexBuffer: ShortBuffer? = null
    private var wireframeIndexBuffer: ShortBuffer? = null
    private var indexCount = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        planets.forEach { it.texture.load() }

        programSkySphere = initializeShaderProgram(
            vertexShaderSkySphere,
            fragmentShaderSkySphereNoTexture
        )
        programPlanets = initializeShaderProgram(vertexShaderPlanets, fragmentShaderPlanets)

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
        Matrix.perspectiveM(projectionMatrix, 0, 45f, aspectRatio, 0.1f, 500f)
        updateViewMatrix()
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        updateViewMatrix()
        drawGridLines()
        renderPlanets()
    }

    private fun drawGridLines() {
        GLES20.glUseProgram(programSkySphere)

        val mvpMatrixHandle = GLES20.glGetUniformLocation(programSkySphere, "uMVPMatrix")

        // Двигаем сферу в позицию камеры
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, eyeX, eyeY, eyeZ) // Двигаем к камере

        // Создаём итоговую матрицу
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        val positionHandle = GLES20.glGetAttribLocation(programSkySphere, "aPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 5 * 4, vertexBuffer)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        val colorHandle = GLES20.glGetUniformLocation(programSkySphere, "uColor")
        GLES20.glUniform4f(colorHandle, 0f, 0f, 0f, 1f) // Чёрный цвет, полностью непрозрачный

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
    }


    private fun renderPlanets() {
        planets.forEach { planet ->
            // Конвертация горизонтальных координат в декартовы
            val altitudeRadians = Math.toRadians(planet.altitudeDegrees)
            val azimuthRadians = Math.toRadians(planet.azimuthDegrees)

            val distanceMultiplier = 30.0 // Умножаем на 10 для увеличения расстояния
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

    private fun updateViewMatrix() {
        val radAzimuth = Math.toRadians(azimuth.toDouble()).toFloat()
        val radAltitude = Math.toRadians(altitude.toDouble()).toFloat()

        // Обновляем координаты камеры
        eyeX = (distance * cos(radAltitude) * sin(radAzimuth))
        eyeY = (distance * sin(radAltitude))
        eyeZ = (distance * cos(radAltitude) * cos(radAzimuth))

        Matrix.setLookAtM(
            viewMatrix, 0,
            eyeX, eyeY, eyeZ,  // Камера
            0f, 0f, 0f,        // Смотрим в центр сцены
            0f, 1f, 0f         // Вверх
        )
    }

    private fun initializeShaderProgram(
        vertexShaderCode: String,
        fragmentShaderCode: String,
    ): Int {
        val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader =
            compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        val program = createProgram(vertexShader, fragmentShader)

        GLES20.glUseProgram(program)

        val textureHandle = GLES20.glGetUniformLocation(program, "uTexture")
        GLES20.glUniform1i(
            textureHandle,
            0
        ) // Указываем, что текстура будет использоваться в unit 0

        return program
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                previousX = event.x
                previousY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.x - previousX
                val deltaY = event.y - previousY

                val sensitivity = 0.05f
                azimuth += deltaX * sensitivity
                altitude -= deltaY * sensitivity

                // Ограничение угла поворота вверх-вниз (-85° до 85°)
                altitude = altitude.coerceIn(-85f, 85f)

                previousX = event.x
                previousY = event.y
            }
        }
        return true
    }

    fun setScaleDetector(context: Context) {
        scaleDetector = ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    distance /= detector.scaleFactor
                    distance = distance.coerceIn(1f, 50f) // Ограничиваем диапазон приближения
                    return true
                }
            })
    }
}
