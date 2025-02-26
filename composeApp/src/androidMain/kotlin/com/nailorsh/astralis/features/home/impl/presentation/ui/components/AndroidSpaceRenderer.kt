@file:OptIn(ExperimentalUnsignedTypes::class)

package com.nailorsh.astralis.features.home.impl.presentation.ui.components


import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.danielgergely.kgl.KglAndroid
import com.nailorsh.astralis.features.home.impl.presentation.data.model.BodyWithPosition
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class AndroidSpaceRenderer(
    context: Context,
    planets: List<BodyWithPosition>
) : GLSurfaceView.Renderer {
    private val renderer = SpaceRenderer(planets, KglAndroid)

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val gravity = FloatArray(3) // Данные с акселерометра
    private val geomagnetic = FloatArray(3) // Данные с магнитометра

    private val smoothingFactor = 0.1f // Чем меньше, тем плавнее, но медленнее реагирует
    private var isListening = false

    var previousX = 0f
    var previousY = 0f

    private lateinit var scaleDetector: ScaleGestureDetector

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
                renderer.azimuth =
                    renderer.azimuth * (1 - smoothingFactor) + newAzimuth * smoothingFactor
                renderer.altitude =
                    renderer.altitude * (1 - smoothingFactor) + newAltitude * smoothingFactor
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

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) = renderer.init()

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) =
        renderer.update(width, height)

    override fun onDrawFrame(gl: GL10?) = renderer.draw()

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
                renderer.azimuth += deltaX * sensitivity
                renderer.altitude -= deltaY * sensitivity

                // Ограничение угла поворота вверх-вниз (-85° до 85°)
                renderer.altitude = renderer.altitude.coerceIn(-85f, 85f)

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
                    renderer.distance /= detector.scaleFactor
                    renderer.distance =
                        renderer.distance.coerceIn(1f, 50f) // Ограничиваем диапазон приближения
                    return true
                }
            })
    }
}
