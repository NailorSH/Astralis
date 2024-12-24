package com.nailorsh.astralis.core.utils.graphics

import android.opengl.GLES20

fun compileShader(type: Int, shaderCode: String): Int {
    val shader = GLES20.glCreateShader(type)

    if (shader == 0) {
        throw RuntimeException("Error creating shader.")
    }

    GLES20.glShaderSource(shader, shaderCode)
    GLES20.glCompileShader(shader)

    val compiled = IntArray(1)
    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)

    if (compiled[0] == 0) {
        val error = GLES20.glGetShaderInfoLog(shader)
        GLES20.glDeleteShader(shader)
        throw RuntimeException("Shader compilation failed: $error")
    }

    return shader
}

fun createProgram(vertexShader: Int, fragmentShader: Int): Int {
    val program = GLES20.glCreateProgram()

    if (program == 0) {
        throw RuntimeException("Error creating program.")
    }

    GLES20.glAttachShader(program, vertexShader)
    GLES20.glAttachShader(program, fragmentShader)
    GLES20.glLinkProgram(program)

    val linked = IntArray(1)
    GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linked, 0)

    if (linked[0] == 0) {
        val error = GLES20.glGetProgramInfoLog(program)
        GLES20.glDeleteProgram(program)
        throw RuntimeException("Program linking failed: $error")
    }

    return program
}