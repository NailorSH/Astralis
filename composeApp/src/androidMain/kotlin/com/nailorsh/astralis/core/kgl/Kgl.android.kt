package com.nailorsh.astralis.core.kgl

import com.danielgergely.kgl.Buffer
import com.danielgergely.kgl.GL
import com.danielgergely.kgl.Kgl

actual fun Kgl.bufferSubData(target: Int, offset: Int, size: Int, sourceData: Buffer) {
    sourceData.withJavaBuffer { javaBuffer ->
        GL.glBufferSubData(target, offset, size, javaBuffer)
    }
}

actual fun Kgl.vertexAttribPointer(
    location: Int,
    size: Int,
    type: Int,
    normalized: Boolean,
    stride: Int,
    buffer: Buffer
) {
    buffer.withJavaBuffer { javaBuffer ->
        GL.glVertexAttribPointer(location, size, type, normalized, stride, javaBuffer)
    }
}

actual fun Kgl.drawElements(
    mode: Int,
    count: Int,
    type: Int,
    indices: Buffer
) {
    indices.withJavaBuffer { javaBuffer ->
        GL.glDrawElements(mode, count, type, javaBuffer)
    }
}