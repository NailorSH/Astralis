@file:OptIn(ExperimentalUnsignedTypes::class)

package com.nailorsh.astralis.features.home.impl.presentation.ui.components


import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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

private val vertexShaderStars = """
        attribute vec4 aPosition;
        uniform mat4 uMVPMatrix;
        void main() {
            gl_Position = uMVPMatrix * aPosition;
        }
    """.trimIndent()

private val fragmentShaderStars = """
        precision mediump float;
        uniform vec4 uColor;
        void main() {
            gl_FragColor = uColor;
        }
    """.trimIndent()

class SpaceRenderer(
    context: Context,
    private val planets: List<BodyWithPosition>
) : GLSurfaceView.Renderer {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val gravity = FloatArray(3) // Данные с акселерометра
    private val geomagnetic = FloatArray(3) // Данные с магнитометра

    private val smoothingFactor = 0.1f // Чем меньше, тем плавнее, но медленнее реагирует
    private var isListening = false

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return

            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    lowPassFilter(event.values, gravity)
                }

                Sensor.TYPE_MAGNETIC_FIELD -> {
                    lowPassFilter(event.values, geomagnetic)
                }
            }

            val rotationMatrix = FloatArray(9)
            val inclinationMatrix = FloatArray(9)

            if (SensorManager.getRotationMatrix(
                    rotationMatrix,
                    inclinationMatrix,
                    gravity,
                    geomagnetic
                )
            ) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)

                // Преобразуем радианы в градусы
                val newAzimuth = -Math.toDegrees(orientation[0].toDouble()).toFloat()
                val newAltitude = Math.toDegrees(orientation[1].toDouble()).toFloat()

                // Применяем сглаживание
                azimuth = azimuth * (1 - smoothingFactor) + newAzimuth * smoothingFactor
                altitude = altitude * (1 - smoothingFactor) + newAltitude * smoothingFactor
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    // Функция сглаживания входных данных (low-pass filter)
    private fun lowPassFilter(input: FloatArray, output: FloatArray) {
        for (i in input.indices) {
            output[i] = output[i] * (1 - smoothingFactor) + input[i] * smoothingFactor
        }
    }

    fun startListening() {
        if (!isListening) {
            isListening = true
            accelerometer?.let {
                sensorManager.registerListener(
                    sensorEventListener,
                    it,
                    SensorManager.SENSOR_DELAY_UI
                )
            }
            magnetometer?.let {
                sensorManager.registerListener(
                    sensorEventListener,
                    it,
                    SensorManager.SENSOR_DELAY_UI
                )
            }
        }
    }

    fun stopListening() {
        if (isListening) {
            isListening = false
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

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
    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)

    // Буферы для данных сферы
    private var vertexBuffer: FloatBuffer? = null
    private var indexBuffer: ShortBuffer? = null
    private var wireframeIndexBuffer: ShortBuffer? = null
    private var indexCount = 0

    private val stars = generateRandomStars(100)

    private lateinit var planetRenderer: PlanetRenderer
    private lateinit var starRenderer: StarRenderer
    private lateinit var gridRenderer: GridRenderer

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Инициализация OpenGL и шейдеров
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        planets.forEach { it.texture.load() }

        val programSkySphere =
            initializeShaderProgram(vertexShaderSkySphere, fragmentShaderSkySphereNoTexture)
        val programPlanets = initializeShaderProgram(vertexShaderPlanets, fragmentShaderPlanets)
        val programStars = initializeShaderProgram(vertexShaderStars, fragmentShaderStars)

        generateSphereData(40, 40)

        // Инициализация рендереров
        planetRenderer = PlanetRenderer(programPlanets, vertexBuffer, indexBuffer, indexCount)
        starRenderer = StarRenderer(programStars)
        gridRenderer = GridRenderer(programSkySphere, vertexBuffer, wireframeIndexBuffer)
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

        // Рендеринг сетки
        gridRenderer.drawGridLines(viewMatrix, projectionMatrix, eyeX, eyeY, eyeZ)

        // Рендеринг планет
        planetRenderer.renderPlanets(planets, viewMatrix, projectionMatrix)

        // Рендеринг звёзд
        starRenderer.renderStars(stars, viewMatrix, projectionMatrix)
    }

    private fun generateSphereData(latitudeBands: Int, longitudeBands: Int, radius: Double = 1.0) {
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

                val x = cosPhi * sinTheta * radius
                val y = cosTheta * radius
                val z = sinPhi * sinTheta * radius
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
            0f, 0f, 1f,        // Смотрим в центр сцены
            0f, 1f, 0f         // Вверх
        )
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

fun initializeShaderProgram(
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
