package com.nailorsh.astralis.core.kgl

import com.danielgergely.kgl.Buffer
import com.danielgergely.kgl.GL
import com.danielgergely.kgl.Kgl

actual fun Kgl.bufferSubData(target: Int, offset: Int, size: Int, sourceData: Buffer) {
    sourceData.withJavaBuffer { javaBuffer ->
        GL.glBufferSubData(target, offset, size, javaBuffer)
    }
}