package com.nailorsh.astralis.core.utils.graphics.math

import androidx.compose.ui.graphics.Matrix
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Define a projection matrix in terms of a field of view angle, an
 * aspect ratio, and z clip planes
 * @param m the float array that holds the perspective matrix
 * @param offset the offset into float array m where the perspective
 * matrix data is written
 * @param fovy field of view in y direction, in degrees
 * @param aspect width to height aspect ratio of the viewport
 * @param zNear the near clipping plane distance
 * @param zFar the far clipping plane distance
 */
fun Matrix.Companion.perspectiveM(
    m: FloatArray,
    offset: Int,
    fovy: Float,
    aspect: Float,
    zNear: Float,
    zFar: Float
) {
    val f = 1f / tan(fovy * (PI.toFloat() / 360f))
    val rangeReciprocal = 1f / (zNear - zFar)

    m[offset + 0] = f / aspect
    m[offset + 1] = 0f
    m[offset + 2] = 0f
    m[offset + 3] = 0f

    m[offset + 4] = 0f
    m[offset + 5] = f
    m[offset + 6] = 0f
    m[offset + 7] = 0f

    m[offset + 8] = 0f
    m[offset + 9] = 0f
    m[offset + 10] = (zFar + zNear) * rangeReciprocal
    m[offset + 11] = -1f

    m[offset + 12] = 0f
    m[offset + 13] = 0f
    m[offset + 14] = 2f * zFar * zNear * rangeReciprocal
    m[offset + 15] = 0f
}

/**
 * Define a viewing transformation in terms of an eye point, a center of view, and an up vector.
 *
 * @param rm The array to store the resulting matrix.
 * @param rmOffset The offset into the array where the matrix data starts.
 * @param eyeX The X coordinate of the eye point.
 * @param eyeY The Y coordinate of the eye point.
 * @param eyeZ The Z coordinate of the eye point.
 * @param centerX The X coordinate of the center of view.
 * @param centerY The Y coordinate of the center of view.
 * @param centerZ The Z coordinate of the center of view.
 * @param upX The X component of the up vector.
 * @param upY The Y component of the up vector.
 * @param upZ The Z component of the up vector.
 */
fun Matrix.Companion.setLookAtM(
    rm: FloatArray, rmOffset: Int,
    eyeX: Float, eyeY: Float, eyeZ: Float,
    centerX: Float, centerY: Float, centerZ: Float,
    upX: Float, upY: Float, upZ: Float
) {
    // Calculate the forward vector (f)
    var fx = centerX - eyeX
    var fy = centerY - eyeY
    var fz = centerZ - eyeZ

    // Normalize the forward vector
    val rlf = 1f / length(fx, fy, fz)
    fx *= rlf
    fy *= rlf
    fz *= rlf

    // Calculate the side vector (s = f x up) (x means "cross product")
    var sx = fy * upZ - fz * upY
    var sy = fz * upX - fx * upZ
    var sz = fx * upY - fy * upX

    // Normalize the side vector
    val rls = 1f / length(sx, sy, sz)
    sx *= rls
    sy *= rls
    sz *= rls

    // Calculate the up vector (u = s x f)
    val ux = sy * fz - sz * fy
    val uy = sz * fx - sx * fz
    val uz = sx * fy - sy * fx

    // Fill the rotation part of the matrix
    rm[rmOffset + 0] = sx
    rm[rmOffset + 1] = ux
    rm[rmOffset + 2] = -fx
    rm[rmOffset + 3] = 0f

    rm[rmOffset + 4] = sy
    rm[rmOffset + 5] = uy
    rm[rmOffset + 6] = -fy
    rm[rmOffset + 7] = 0f

    rm[rmOffset + 8] = sz
    rm[rmOffset + 9] = uz
    rm[rmOffset + 10] = -fz
    rm[rmOffset + 11] = 0f

    rm[rmOffset + 12] = 0f
    rm[rmOffset + 13] = 0f
    rm[rmOffset + 14] = 0f
    rm[rmOffset + 15] = 1f

    // Translate the matrix to account for the eye position
    translateM(rm, rmOffset, -eyeX, -eyeY, -eyeZ)
}

/**
 * Computes the length of a vector
 *
 * @param x x coordinate of a vector
 * @param y y coordinate of a vector
 * @param z z coordinate of a vector
 * @return the length of a vector
 */
private fun Matrix.Companion.length(x: Float, y: Float, z: Float): Float {
    return sqrt(x * x + y * y + z * z)
}

/**
 * Translates matrix m by x, y, and z in place.
 *
 * @param m The matrix to translate.
 * @param mOffset The offset into the matrix array where the matrix starts.
 * @param x The translation factor along the X axis.
 * @param y The translation factor along the Y axis.
 * @param z The translation factor along the Z axis.
 */
fun Matrix.Companion.translateM(
    m: FloatArray, mOffset: Int,
    x: Float, y: Float, z: Float
) {
    for (i in 0 until 4) {
        val mi = mOffset + i
        m[12 + mi] += m[mi] * x + m[4 + mi] * y + m[8 + mi] * z
    }
}

/**
 * Translates matrix m by x, y, and z, putting the result in tm.
 *
 * @param tm The array to store the resulting translated matrix.
 * @param tmOffset The offset into the array where the result matrix starts.
 * @param m The source matrix.
 * @param mOffset The offset into the source matrix where it starts.
 * @param x The translation factor along the X axis.
 * @param y The translation factor along the Y axis.
 * @param z The translation factor along the Z axis.
 */
fun Matrix.Companion.translateM(
    tm: FloatArray, tmOffset: Int,
    m: FloatArray, mOffset: Int,
    x: Float, y: Float, z: Float
) {
    // Copy the first 12 elements of the source matrix to the result matrix
    for (i in 0 until 12) tm[tmOffset + i] = m[mOffset + i]

    // Apply translation to the last column of the matrix
    for (i in 0 until 4) {
        val tmi = tmOffset + i
        val mi = mOffset + i
        tm[12 + tmi] = m[mi] * x + m[4 + mi] * y + m[8 + mi] * z + m[12 + mi]
    }
}

/**
 * Sets matrix m to the identity matrix.
 * @param sm returns the result
 * @param smOffset index into sm where the result matrix starts
 */
fun Matrix.Companion.setIdentityM(sm: FloatArray, smOffset: Int) {
    for (i in 0 until 16) sm[smOffset + i] = 0f  // Заполняем матрицу нулями
    for (i in 0 until 16 step 5) sm[smOffset + i] = 1f  // Устанавливаем единицы на диагонали
}

/**
 * Multiply two 4x4 matrices together and store the result in a third 4x4 matrix.
 * In matrix notation: result = lhs x rhs.
 *
 * @param result The float array that holds the result.
 * @param resultOffset The offset into the result array where the result is stored.
 * @param lhs The float array that holds the left-hand-side matrix.
 * @param lhsOffset The offset into the lhs array where the lhs is stored.
 * @param rhs The float array that holds the right-hand-side matrix.
 * @param rhsOffset The offset into the rhs array where the rhs is stored.
 *
 * @throws IllegalArgumentException if result, lhs, or rhs are null, or if
 * resultOffset + 16 > result.length or lhsOffset + 16 > lhs.length or
 * rhsOffset + 16 > rhs.length.
 */
fun Matrix.Companion.multiplyMM(
    result: FloatArray, resultOffset: Int,
    lhs: FloatArray, lhsOffset: Int,
    rhs: FloatArray, rhsOffset: Int
) {
    require(resultOffset + 16 <= result.size) { "Result array is too small." }
    require(lhsOffset + 16 <= lhs.size) { "LHS array is too small." }
    require(rhsOffset + 16 <= rhs.size) { "RHS array is too small." }

    // Временный массив для хранения результата
    val temp = FloatArray(16)

    // Умножение матриц
    for (i in 0 until 4) {
        for (j in 0 until 4) {
            var sum = 0f
            for (k in 0 until 4) {
                sum += lhs[lhsOffset + i * 4 + k] * rhs[rhsOffset + k * 4 + j]
            }
            temp[i * 4 + j] = sum
        }
    }

    temp.copyInto(result, resultOffset, 0, 16)
}

/**
 * Scales matrix m in place by sx, sy, and sz.
 *
 * @param m The matrix to scale.
 * @param mOffset The index into m where the matrix starts.
 * @param x The scale factor along the X axis.
 * @param y The scale factor along the Y axis.
 * @param z The scale factor along the Z axis.
 */
fun Matrix.Companion.scaleM(m: FloatArray, mOffset: Int, x: Float, y: Float, z: Float) {
    for (i in 0 until 4) {
        val mi = mOffset + i
        m[mi] *= x
        m[4 + mi] *= y
        m[8 + mi] *= z
    }
}