package com.nailorsh.astralis.core.kgl

import com.danielgergely.kgl.Buffer
import com.danielgergely.kgl.Kgl
import com.danielgergely.kgl.Program

expect fun Kgl.bufferSubData(target: Int, offset: Int, size: Int, sourceData: Buffer)

fun Kgl.setAttributeBuffer(location: Int, type: Int, offset: Int, tupleSize: Int, stride: Int = 0) {
    if (location != -1) vertexAttribPointer(location, tupleSize, type, true, stride, offset)
}

fun Kgl.enableAttributeArray(location: Int) {
    if (location != -1) enableVertexAttribArray(location)
}

fun Kgl.setUniformValue(program: Program, name: String, value: FloatArray) {
    val location = getUniformLocation(program, name)
    location?.let { uniformMatrix4fv(it, false, value) }
}

expect fun Kgl.vertexAttribPointer(
    location: Int,
    size: Int,
    type: Int,
    normalized: Boolean,
    stride: Int,
    buffer: Buffer
)

expect fun Kgl.drawElements(mode: Int, count: Int, type: Int, indices: Buffer)