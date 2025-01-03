package com.nailorsh.astralis


import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.view.GestureDetector
import android.view.MotionEvent
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

val fragmentShaderSkySphere = """
    precision mediump float;
    uniform sampler2D uTexture; // Текстура
    varying vec2 vTexCoord; // Текстурные координаты

    void main() {
        gl_FragColor = texture2D(uTexture, vTexCoord); // Получаем цвет из текстуры по текстурным координатам
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

data class BodyWithPosition(
    val id: String,
    val azimuthDegrees: Double,
    val altitudeDegrees: Double,
    val distanceFromEarthAU: Double,
    val textureId: Int
)

class SphereRenderer2(private val context: Context) : GLSurfaceView.Renderer {
    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                Logger.d("GestureDetector") { "onScroll: distanceX=$distanceX, distanceY=$distanceY" }

                // Регулировка чувствительности
                val sensitivity = 0.2f
                azimuth -= distanceX * sensitivity
                altitude -= distanceY * sensitivity

                // Ограничение вертикального угла (камера не может смотреть "вверх ногами")
                altitude = altitude.coerceIn(-90f, 90f)

                return true
            }
        }
    )

    private var useTexSky = false
    private var wireframeMode = true // Флаг для режима wireframe

    private val planets = mutableListOf<BodyWithPosition>()

    private var azimuth = 0f // Угол поворота влево-вправо
    private var altitude = 0f // Угол поворота вверх-вниз

    private var previousX = 0f
    private var previousY = 0f

    // Матрицы
    private val modelMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    // OpenGL ресурсы
    private var programSkySphere = 0
    private var programPlanets = 0
    private var textureId = 0

    // Буферы для данных сферы
    private var vertexBuffer: FloatBuffer? = null
    private var indexBuffer: ShortBuffer? = null
    private var wireframeIndexBuffer: ShortBuffer? = null
    private var indexCount = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        planets.addAll(
            listOf(
                BodyWithPosition(
                    id = "sun",
                    azimuthDegrees = 86.98,
                    altitudeDegrees = -30.30,
                    distanceFromEarthAU = 0.98356,
                    textureId = loadTexture(context, R.drawable.sun)
                ),
//                BodyWithPosition(
//                    id = "moon",
//                    azimuthDegrees = 138.79,
//                    altitudeDegrees = 12.93,
//                    distanceFromEarthAU = 0.00269,
//                    textureId = loadTexture(context, R.drawable.moon)
//                ),
                BodyWithPosition(
                    id = "mercury",
                    azimuthDegrees = 103.87,
                    altitudeDegrees = -14.48,
                    distanceFromEarthAU = 1.01530,
                    textureId = loadTexture(context, R.drawable.mercury)
                ),
                BodyWithPosition(
                    id = "venus",
                    azimuthDegrees = 29.51,
                    altitudeDegrees = -47.52,
                    distanceFromEarthAU = 0.80160,
                    textureId = loadTexture(context, R.drawable.venus)
                ),
                BodyWithPosition(
                    id = "mars",
                    azimuthDegrees = 233.92,
                    altitudeDegrees = 47.67,
                    distanceFromEarthAU = 0.67816,
                    textureId = loadTexture(context, R.drawable.mars)
                ),
                BodyWithPosition(
                    id = "jupiter",
                    azimuthDegrees = 283.23,
                    altitudeDegrees = 17.62,
                    distanceFromEarthAU = 4.14377,
                    textureId = loadTexture(context, R.drawable.jupiter)
                ),
                BodyWithPosition(
                    id = "saturn",
                    azimuthDegrees = 356.39,
                    altitudeDegrees = -41.98,
                    distanceFromEarthAU = 9.91938,
                    textureId = loadTexture(context, R.drawable.saturn)
                ),
                BodyWithPosition(
                    id = "uranus",
                    azimuthDegrees = 298.12,
                    altitudeDegrees = 3.83,
                    distanceFromEarthAU = 18.78885,
                    textureId = loadTexture(context, R.drawable.uranus)
                ),
                BodyWithPosition(
                    id = "neptune",
                    azimuthDegrees = 342.05,
                    altitudeDegrees = -34.85,
                    distanceFromEarthAU = 29.98838,
                    textureId = loadTexture(context, R.drawable.neptune)
                ),
                BodyWithPosition(
                    id = "pluto",
                    azimuthDegrees = 58.05,
                    altitudeDegrees = -46.08,
                    distanceFromEarthAU = 36.03690,
                    textureId = loadTexture(context, R.drawable.pluto)
                ),
            )
        )

        programSkySphere = initializeShaderProgram(
            vertexShaderSkySphere,
            if (useTexSky) fragmentShaderSkySphere else fragmentShaderSkySphereNoTexture
        )
        programPlanets = initializeShaderProgram(vertexShaderPlanets, fragmentShaderPlanets)

        // Загрузка текстуры
        // https://www.flickr.com/photos/nasawebbtelescope/54183500660/
        textureId = loadTexture(context, R.drawable.space)

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
        Matrix.perspectiveM(projectionMatrix, 0, 45f, aspectRatio, 0.1f, 500f)

        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 0f, 0f,  // Камера находится в центре сферы
            0f, 0f, -1f, // Смотрит вперёд по оси -Z
            0f, 1f, 0f   // Верхняя часть направлена вдоль оси Y
        )
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Обновляем матрицу вида
        updateViewMatrix()

        // Отрисовка небесной сферы
        Matrix.setIdentityM(modelMatrix, 0)

        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        drawSphere()

        // Отрисовка планет
        renderPlanets()
    }

    private fun drawSphere() {
        GLES20.glUseProgram(programSkySphere)

        val mvpMatrixHandle = GLES20.glGetUniformLocation(programSkySphere, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        val positionHandle = GLES20.glGetAttribLocation(programSkySphere, "aPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 5 * 4, vertexBuffer)

        if (useTexSky) {
            val texCoordHandle = GLES20.glGetAttribLocation(programSkySphere, "aTexCoord")
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
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

            val colorHandle = GLES20.glGetUniformLocation(programSkySphere, "uColor")
            GLES20.glUniform4f(colorHandle, 0f, 0f, 0f, 1f) // Устанавливаем цвет (прозрачный)
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
        if (useTexSky) {
            GLES20.glDisableVertexAttribArray(
                GLES20.glGetAttribLocation(
                    programSkySphere,
                    "aTexCoord"
                )
            )
        }
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

            Logger.d("PlanetPosition") {
                "Planet ${planet.id}: x=$x, y=$y, z=$z"
            }

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

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, planet.textureId)

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
        val eye = floatArrayOf(0f, 0f, 0f) // Позиция камеры
        val center = FloatArray(3) // Точка, на которую смотрит камера
        val up = floatArrayOf(0f, 1f, 1f) // Вектор "вверх"

        // Угол поворота камеры
        val radAzimuth = Math.toRadians(azimuth.toDouble())
        val radAltitude = Math.toRadians(altitude.toDouble())

        // Вычисляем направление взгляда камеры
        // Перевод сферических координат в декартовые
        center[0] = (sin(radAzimuth) * cos(radAltitude)).toFloat()
        center[1] = sin(radAltitude).toFloat()
        center[2] = (cos(radAzimuth) * cos(radAltitude)).toFloat()

        // Вычисляем матрицу вида
        Matrix.setLookAtM(
            viewMatrix,
            0,
            eye[0], eye[1], eye[2],
            center[0], center[1], center[2],
            up[0], up[1], up[2]
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

    fun toggleWireframeMode() {
        wireframeMode = !wireframeMode
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Сохраняем начальные координаты касания
                previousX = event.x
                previousY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                // Вычисляем разницу координат
                val deltaX = event.x - previousX
                val deltaY = event.y - previousY

                // Обновляем углы камеры
                val sensitivity = 0.5f
                azimuth += deltaX * sensitivity
                altitude -= deltaY * sensitivity

                // Ограничиваем вертикальный угол (чтобы избежать переворота)
                altitude = altitude.coerceIn(-89f, 89f)

                // Сохраняем текущие координаты как предыдущие
                previousX = event.x
                previousY = event.y
            }
        }
        return true // Возвращаем true, чтобы событие было обработано
    }
}
