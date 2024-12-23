package com.nailorsh.astralis

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

class PlanetVisualizationView(context: Context) : GLSurfaceView(context) {
    private val renderer: PlanetRenderer

    init {
        setEGLContextClientVersion(2)
        renderer = PlanetRenderer()
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}

data class AstronomicalObject(
    val name: String,
    val azimuth: Float,  // азимут
    val altitude: Float, // высота
    val distance: Float, // расстояние (в астрономических единицах)
    val magnitude: Float // магнитуда
)

class PlanetRenderer : GLSurfaceView.Renderer {

    private val planets = listOf(
        AstronomicalObject("Sun", 131.21f, 10.04f, 0.98378f, -26.77762f),
        AstronomicalObject("Moon", 56.79f, -43.26f, 0.00270f, -9.90583f),
        AstronomicalObject("Mercury", 131.99f, 8.75f, 1.44663f, -1.31155f)
    )
    private val sphereRenderer = SphereRenderer()

    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        sphereRenderer.initialize()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 100f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 1f, 0f)

        renderAstronomicalObjects(planets)
    }

    private fun drawPlanet(position: FloatArray, scale: Float, brightness: Float) {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, position[0], position[1], position[2])
        Matrix.scaleM(modelMatrix, 0, scale, scale, scale)

        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        sphereRenderer.draw(mvpMatrix)
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
            drawPlanet(position, scale, brightness)
        }
    }
}
