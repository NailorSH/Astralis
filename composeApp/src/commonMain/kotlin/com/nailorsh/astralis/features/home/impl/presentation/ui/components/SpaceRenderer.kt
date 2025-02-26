package com.nailorsh.astralis.features.home.impl.presentation.ui.components

import androidx.compose.ui.graphics.Matrix
import com.danielgergely.kgl.FloatBuffer
import com.danielgergely.kgl.GL_COLOR_BUFFER_BIT
import com.danielgergely.kgl.GL_COMPILE_STATUS
import com.danielgergely.kgl.GL_DEPTH_BUFFER_BIT
import com.danielgergely.kgl.GL_DEPTH_TEST
import com.danielgergely.kgl.GL_FRAGMENT_SHADER
import com.danielgergely.kgl.GL_LINK_STATUS
import com.danielgergely.kgl.GL_VERTEX_SHADER
import com.danielgergely.kgl.IntBuffer
import com.danielgergely.kgl.Kgl
import com.danielgergely.kgl.Program
import com.danielgergely.kgl.Shader
import com.nailorsh.astralis.core.utils.graphics.math.perspectiveM
import com.nailorsh.astralis.core.utils.graphics.math.setLookAtM
import com.nailorsh.astralis.core.utils.graphics.math.toRadians
import com.nailorsh.astralis.features.home.impl.presentation.data.model.BodyWithPosition
import kotlin.math.PI
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
    private val planets: List<BodyWithPosition>,
    private val kgl: Kgl
) {
    var azimuth = 0f // Влево-вправо
    var altitude = 0f // Вверх-вниз
    var distance = 5f // Начальная дистанция (приближение)

    // Переменные для хранения позиции камеры
    private var eyeX = 0f
    private var eyeY = 0f
    private var eyeZ = 0f

    // Матрицы
    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)

    // Буферы для данных сферы
    private var vertexBuffer: FloatBuffer? = null
    private var indexBuffer: IntBuffer? = null
    private var wireframeIndexBuffer: IntBuffer? = null
    private var indexCount = 0

    private lateinit var planetRenderer: PlanetRenderer
    private lateinit var gridRenderer: GridRenderer

    fun init() {
        // Инициализация OpenGL и шейдеров
        kgl.clearColor(0f, 0f, 0f, 0f)
        kgl.enable(GL_DEPTH_TEST)

        planets.forEach { it.texture.load() }

        val programSkySphere =
            initializeShaderProgram(vertexShaderSkySphere, fragmentShaderSkySphereNoTexture)
        val programPlanets = initializeShaderProgram(vertexShaderPlanets, fragmentShaderPlanets)

        generateSphereData(40, 40)

        // Инициализация рендереров
        planetRenderer = PlanetRenderer(programPlanets, vertexBuffer, indexBuffer, indexCount, kgl)
        gridRenderer = GridRenderer(programSkySphere, vertexBuffer, wireframeIndexBuffer, kgl)
    }

    fun update(width: Int, height: Int) {
        kgl.viewport(0, 0, width, height)

        val aspectRatio = width.toFloat() / height
        Matrix.perspectiveM(projectionMatrix, 0, 45f, aspectRatio, 0.1f, 500f)
        updateViewMatrix()
    }

    fun draw() {
        kgl.clear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        updateViewMatrix()

        // Рендеринг сетки
        gridRenderer.drawGridLines(viewMatrix, projectionMatrix, eyeX, eyeY, eyeZ)

        // Рендеринг планет
        planetRenderer.renderPlanets(planets, viewMatrix, projectionMatrix)
    }

    private fun generateSphereData(latitudeBands: Int, longitudeBands: Int, radius: Double = 1.0) {
        val vertices = mutableListOf<Float>()
        val indices = mutableListOf<Int>()
        val wireframeIndices = mutableListOf<Int>()

        for (lat in 0..latitudeBands) {
            val theta = PI * lat / latitudeBands
            val sinTheta = sin(theta)
            val cosTheta = cos(theta)

            for (lon in 0..longitudeBands) {
                val phi = 2 * PI * lon / longitudeBands
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
                val first = lat * (longitudeBands + 1) + lon
                val second = first + longitudeBands + 1

                // Для триангуляции
                indices.add(first)
                indices.add(first + 1)
                indices.add(second)

                indices.add(second)
                indices.add(first + 1)
                indices.add(second + 1)

                // Wireframe indices
                wireframeIndices.add(first)
                wireframeIndices.add(second)
                wireframeIndices.add(first)
                wireframeIndices.add(first + 1)
                wireframeIndices.add(second)
                wireframeIndices.add(second + 1)
            }
        }

        indexCount = indices.size

        // Создание буфера вершин
        vertexBuffer = FloatBuffer(vertices.toTypedArray())

        // Создание буфера индексов
        indexBuffer = IntBuffer(indices.toTypedArray())
        wireframeIndexBuffer = IntBuffer(wireframeIndices.toTypedArray())
    }

    private fun updateViewMatrix() {
        val radAzimuth = azimuth.toRadians()
        val radAltitude = altitude.toRadians()

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

    private fun initializeShaderProgram(
        vertexShaderCode: String,
        fragmentShaderCode: String,
    ): Program {
        val vertexShader = compileShader(GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader =
            compileShader(GL_FRAGMENT_SHADER, fragmentShaderCode)

        val program = createProgram(vertexShader, fragmentShader)

        kgl.useProgram(program)

        val textureHandle = kgl.getUniformLocation(program, "uTexture")
        textureHandle?.let { kgl.uniform1i(it, 0) }

        return program
    }

    private fun compileShader(type: Int, shaderCode: String): Shader {
        val shader = kgl.createShader(type) ?: throw RuntimeException("Error creating shader.")

        kgl.shaderSource(shader, shaderCode)
        kgl.compileShader(shader)

        val compiled = kgl.getShaderParameter(shader, GL_COMPILE_STATUS)

        if (compiled == 0) {
            val error = kgl.getShaderInfoLog(shader)
            kgl.deleteShader(shader)
            throw RuntimeException("Shader compilation failed: $error")
        }

        return shader
    }

    private fun createProgram(vertexShader: Shader, fragmentShader: Shader): Program {
        val program = kgl.createProgram() ?: throw RuntimeException("Error creating program.")

        kgl.attachShader(program, vertexShader)
        kgl.attachShader(program, fragmentShader)
        kgl.linkProgram(program)

        val linked = kgl.getProgramParameter(program, GL_LINK_STATUS)

        if (linked == 0) {
            val error = kgl.getProgramInfoLog(program)
            kgl.deleteProgram(program)
            throw RuntimeException("Program linking failed: $error")
        }

        return program
    }
}