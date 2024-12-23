package com.nailorsh.astralis

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import co.touchlab.kermit.Logger
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

class SpaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: SpaceRenderer

    init {
        setEGLContextClientVersion(2)
        renderer = SpaceRenderer(context)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        return renderer.onTouchEvent(event)
    }
}

data class AstronomicalObject(
    val name: String,
    val azimuth: Float,  // азимут
    val altitude: Float, // высота
    val distance: Float, // расстояние (в астрономических единицах)
    val magnitude: Float, // магнитуда
    val color: GlColor // цвет (RGBA)
)

enum class GlColor(val rgba: FloatArray) {
    YELLOW(floatArrayOf(1.0f, 1.0f, 0f, 1f)),
    GREY(floatArrayOf(0.8f, 0.8f, 0.8f, 1f)),
    GREY2(floatArrayOf(0.5f, 0.5f, 0.5f, 1f))
}

class SpaceRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private val planets = listOf(
        AstronomicalObject(
            "Sun",
            131.21f,
            10.04f,
            0.98378f,
            -26.77762f,
            GlColor.YELLOW
        ),  // Желтый для Солнца
        AstronomicalObject(
            "Moon",
            56.79f,
            -43.26f,
            0.00270f,
            -9.90583f,
            GlColor.GREY
        ), // Серый для Луны
        AstronomicalObject(
            "Mercury",
            131.99f,
            8.75f,
            1.44663f,
            -1.31155f,
            GlColor.GREY2
        )  // Серый для Меркурия
    )

    private val sphereRenderer = SphereRenderer()
    private val viewProjectionMatrix = FloatArray(16)
    private var scaleFactor = 0f  // Начальный масштаб
    private var scaleGestureDetector: ScaleGestureDetector

    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private var azimuth: Float = 0f  // Поворот камеры по горизонтали (вправо-влево)
    private var altitude: Float = 0f  // Поворот камеры по вертикали (вверх-вниз)
    private var lastX: Float = 0f
    private var lastY: Float = 0f
    private val sensitivity = 0.7f  // Чувствительность к повороту

    init {
        scaleGestureDetector = ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    return onScaleEvent(detector.scaleFactor)
                }
            })
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        sphereRenderer.initialize()
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val aspectRatio = width.toFloat() / height
        Matrix.perspectiveM(
            projectionMatrix,
            0,
            45f,
            aspectRatio,
            1f,
            1000f
        ) // Проекционная матрица
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Обновляем матрицу вида с учетом углов поворота
        val eyeX = 0f
        val eyeY = 0f
        val eyeZ = scaleFactor  // Начальная позиция камеры

        val radAzimuth = Math.toRadians(azimuth.toDouble())
        val radAltitude = Math.toRadians(altitude.toDouble())

        val centerX = (cos(radAltitude) * sin(radAzimuth)).toFloat() // Направление по оси X
        val centerY = sin(radAltitude).toFloat()                  // Направление по оси Y
        val centerZ = (cos(radAltitude) * cos(radAzimuth)).toFloat() // Направление по оси Z

        Matrix.setLookAtM(
            viewMatrix, 0,
            eyeX, eyeY, eyeZ,
            centerX, centerY, centerZ,
            0f, 1f, 0f
        )

        // Рисуем фон (звезды)
        // Объединяем проекционную и видовую матрицы
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // Рисуем небесную сферу
        drawSkySphere()
    }

    private fun drawSkySphere(
        sphereRadius: Float = 1.5f
    ) {
        // Создаем модельную матрицу с масштабированием до радиуса сферы
        val modelMatrix = FloatArray(16).apply {
            Matrix.setIdentityM(this, 0)
            Matrix.scaleM(this, 0, sphereRadius, sphereRadius, sphereRadius)
        }

        // Объединяем модельную и видо-проекционную матрицы
        val mvpMatrix = FloatArray(16).apply {
            Matrix.multiplyMM(this, 0, viewProjectionMatrix, 0, modelMatrix, 0)
        }

        // Рисуем небесную сферу с голубым цветом
        sphereRenderer.draw(
            mvpMatrix,
            floatArrayOf(0.1f, 0.2f, 0.8f, 1.0f)
        ) // Пример голубого цвета
    }

    private fun drawPlanet(
        position: FloatArray,
        scale: Float,
        brightness: Float,
        color: FloatArray
    ) {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, position[0], position[1], position[2])
        Matrix.scaleM(modelMatrix, 0, scale, scale, scale)

        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        sphereRenderer.draw(mvpMatrix, color)
    }

    private fun sphericalToCartesian(azimuth: Float, altitude: Float, distance: Float): FloatArray {
        val radAzimuth = Math.toRadians(azimuth.toDouble())
        val radAltitude = Math.toRadians(altitude.toDouble())

        val x = (distance * cos(radAltitude) * sin(radAzimuth)).toFloat()
        val y = (distance * sin(radAltitude)).toFloat()
        val z = (distance * cos(radAltitude) * cos(radAzimuth)).toFloat()

        return floatArrayOf(x, y, z)
    }

    private fun renderAstronomicalObjects(objects: List<AstronomicalObject>) {
        objects.forEach { planet ->
            // Преобразуем координаты
            val position = sphericalToCartesian(planet.azimuth, planet.altitude, planet.distance)

            // Масштабируем планету
            val scale = 1 / (planet.distance * 0.1f) // Масштабирование по расстоянию

            // Преобразуем магнитуду в яркость
            val brightness = 1 / (10f * planet.magnitude)

            // Отрисовываем объект
            drawPlanet(position, scale, brightness, planet.color.rgba)
        }
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        Logger.d("onTouchEvent") { "pointerCount: ${event.pointerCount}" }
        Logger.d("onTouchEvent") { "scaleFactor: $scaleFactor" }
        Logger.d("onTouchEvent") { "azimuth: $azimuth" }
        Logger.d("onTouchEvent") { "altitude: $altitude" }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Сохраняем начальные координаты касания при первом касании
                lastX = event.x
                lastY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                // Обработка перемещения пальцев (возможно масштабирование)
                // Получаем разницу с предыдущей позицией
                val dx = event.x - lastX  // Разница по горизонтали
                val dy = event.y - lastY  // Разница по вертикали

                // Обновляем углы поворота камеры
                azimuth += dx * sensitivity  // Поворот по горизонтали
                altitude -= dy * sensitivity  // Поворот по вертикали

                // Ограничение вертикального угла наклона (чтобы не перевернуть камеру)
                altitude = altitude.coerceIn(-90f, 90f)

                // Обновляем позицию для следующего события касания
                lastX = event.x
                lastY = event.y
            }

            MotionEvent.ACTION_UP -> {
                // Можно сбросить или оставить начальные значения, если нужно для следующего события
            }

            MotionEvent.ACTION_CANCEL -> {
                // Событие отмены (если вдруг приложение потеряет фокус)
            }
        }
        return true
    }

    fun onScaleEvent(scale: Float): Boolean {
        if (scaleFactor == 0f) scaleFactor = 1f
        scaleFactor /= scale

        return true
    }
}
