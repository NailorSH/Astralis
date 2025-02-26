package com.nailorsh.astralis.core.kgl

import com.danielgergely.kgl.Buffer
import com.danielgergely.kgl.Kgl

actual fun Kgl.bufferSubData(target: Int, offset: Int, size: Int, sourceData: Buffer) {
    // TODO
}

actual fun Kgl.vertexAttribPointer(
    location: Int,
    size: Int,
    type: Int,
    normalized: Boolean,
    stride: Int,
    buffer: Buffer
) {
    // TODO
}

actual fun Kgl.drawElements(
    mode: Int,
    count: Int,
    type: Int,
    indices: Buffer
) {
    // TODO
}