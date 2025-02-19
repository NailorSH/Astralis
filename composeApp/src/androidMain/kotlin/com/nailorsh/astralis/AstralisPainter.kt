//package com.nailorsh.astralis
//
//import android.opengl.GLES10.GL_LINE_SMOOTH
//import android.opengl.GLES20
//import com.danielgergely.kgl.GL_BLEND
//import com.danielgergely.kgl.GL_CULL_FACE
//import com.danielgergely.kgl.GL_DEPTH_TEST
//import com.danielgergely.kgl.GL_ONE_MINUS_SRC_ALPHA
//import com.danielgergely.kgl.GL_SRC_ALPHA
//import com.danielgergely.kgl.KglAndroid
//
//
//class AstralisPainter(val projector: AstralisProjector) {
//    private var glState = GLState()
//
//    // Set the OpenGL GL_CULL_FACE state, by default face culling is disabled
//    fun setCullFace(enable: Boolean) {
//        if (glState.cullFace != enable) {
//            glState.cullFace = enable
//            if (enable) KglAndroid.enable(GL_CULL_FACE)
//            else KglAndroid.disable(GL_CULL_FACE)
//        }
//    }
//
//    fun setBlending(
//        enableBlending: Boolean,
//        blendSrc: Int = GL_SRC_ALPHA,
//        blendDst: Int = GL_ONE_MINUS_SRC_ALPHA
//    ) {
//        if (enableBlending != glState.blend) {
//            glState.blend = enableBlending
//            if (enableBlending) KglAndroid.enable(GL_BLEND)
//            else KglAndroid.disable(GL_BLEND)
//        }
//        if (enableBlending) {
//            if (blendSrc != glState.blendSrc || blendDst != glState.blendDst) {
//                glState.blendSrc = blendSrc
//                glState.blendDst = blendDst
//                KglAndroid.blendFunc(blendSrc, blendDst)
//            }
//        }
//    }
//
//    private data class GLState(
//        var blend: Boolean = false,
//        var blendSrc: Int = GL_SRC_ALPHA,
//        var blendDst: Int = GL_ONE_MINUS_SRC_ALPHA,
//        var depthTest: Boolean = false,
//        var depthMask: Boolean = false,
//        var cullFace: Boolean = false,
//        var lineSmooth: Boolean = false,
//        var lineWidth: Float = 1f
//    ) {
//        // Applies the values stored here to set the GL state
//        fun apply() {
//            if (blend) KglAndroid.enable(GL_BLEND) else KglAndroid.disable(GL_BLEND)
//            KglAndroid.blendFunc(blendSrc, blendDst)
//
//            if (depthTest) KglAndroid.enable(GL_DEPTH_TEST) else KglAndroid.disable(GL_DEPTH_TEST)
//            GLES20.glDepthMask(depthMask)
//
//            if (cullFace) KglAndroid.enable(GL_CULL_FACE) else KglAndroid.disable(GL_CULL_FACE)
//
//            GLES20.glLineWidth(lineWidth)
//
//            // Проверяем поддержку GL_LINE_SMOOTH (не все OpenGL ES версии поддерживают)
//            if (lineSmooth) KglAndroid.enable(GL_LINE_SMOOTH)
//            else KglAndroid.disable(GL_LINE_SMOOTH)
//        }
//
//        // Resets the state to the default values (like a GLState was newly constructed)
//        // and calls apply()
//        fun reset() {
//            blend = false
//            blendSrc = GL_SRC_ALPHA
//            blendDst = GL_ONE_MINUS_SRC_ALPHA
//            depthTest = false
//            depthMask = false
//            cullFace = false
//            lineSmooth = false
//            lineWidth = 1f
//            apply()
//        }
//    }
//}