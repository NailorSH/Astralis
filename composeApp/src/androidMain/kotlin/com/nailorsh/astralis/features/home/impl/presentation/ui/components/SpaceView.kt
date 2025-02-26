package com.nailorsh.astralis.features.home.impl.presentation.ui.components

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.nailorsh.astralis.features.home.impl.presentation.data.model.BodyWithPosition

class SpaceView(context: Context, planets: List<BodyWithPosition>) : GLSurfaceView(context) {
    private val renderer = AndroidSpaceRenderer(context, planets)

    init {
        setEGLContextClientVersion(2)

        // Включаем прозрачность
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        setZOrderOnTop(true) // Позволяет видеть фон позади

        renderer.setScaleDetector(context)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun updateSensorsState(isSensorsOn: Boolean) {
        if (isSensorsOn) renderer.startListening() else renderer.stopListening()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = renderer.onTouchEvent(event)
}
