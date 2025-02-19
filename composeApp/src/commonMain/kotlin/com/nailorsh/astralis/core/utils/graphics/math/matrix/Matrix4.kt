//package com.nailorsh.astralis.core.utils.graphics.math.matrix
//
//import com.nailorsh.astralis.core.utils.graphics.math.vector.Vector3
//import com.nailorsh.astralis.core.utils.graphics.math.vector.Vector4
//import kotlin.math.cos
//import kotlin.math.sin
//
//typealias Mat4d = Matrix4<Double>
//typealias Mat4f = Matrix4<Float>
//
//class Matrix4<T : Number> {
//    val array: MutableList<Double> = MutableList(16) { 0.0 }
//
//    constructor() {
//        setIdentity()
//    }
//
//    constructor(
//        m00: Number, m01: Number, m02: Number, m03: Number,
//        m10: Number, m11: Number, m12: Number, m13: Number,
//        m20: Number, m21: Number, m22: Number, m23: Number,
//        m30: Number, m31: Number, m32: Number, m33: Number
//    ) {
//        set(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33)
//    }
//
//    constructor(arr: Array<T>) {
//        require(arr.size >= 16) { "Array must contain at least 16 elements" }
//        for (i in 0 until 16) {
//            array[i] = arr[i].toDouble()
//        }
//    }
//
//    constructor(col1: Vector4<T>, col2: Vector4<T>, col3: Vector4<T>, col4: Vector4<T>) {
//        set(
//            col1.x, col2.x, col3.x, col4.x,
//            col1.y, col2.y, col3.y, col4.y,
//            col1.z, col2.z, col3.z, col4.z,
//            col1.w, col2.w, col3.w, col4.w
//        )
//    }
//
//    operator fun get(index: Int): Double = array[index]
//
//    operator fun set(index: Int, value: T) {
//        array[index] = value.toDouble()
//    }
//
//    fun set(
//        m00: Number, m01: Number, m02: Number, m03: Number,
//        m10: Number, m11: Number, m12: Number, m13: Number,
//        m20: Number, m21: Number, m22: Number, m23: Number,
//        m30: Number, m31: Number, m32: Number, m33: Number
//    ) {
//        array[0] = m00.toDouble()
//        array[1] = m01.toDouble()
//        array[2] = m02.toDouble()
//        array[3] = m03.toDouble()
//        array[4] = m10.toDouble()
//        array[5] = m11.toDouble()
//        array[6] = m12.toDouble()
//        array[7] = m13.toDouble()
//        array[8] = m20.toDouble()
//        array[9] = m21.toDouble()
//        array[10] = m22.toDouble()
//        array[11] = m23.toDouble()
//        array[12] = m30.toDouble()
//        array[13] = m31.toDouble()
//        array[14] = m32.toDouble()
//        array[15] = m33.toDouble()
//    }
//
//    fun setIdentity() {
//        set(
//            1, 0, 0, 0,
//            0, 1, 0, 0,
//            0, 0, 1, 0,
//            0, 0, 0, 1
//        )
//    }
//
//    operator fun plus(other: Matrix4<T>): Matrix4<Double> =
//        Matrix4(
//            array[0] + other[0],
//            array[1] + other[1],
//            array[2] + other[2],
//            array[3] + other[3],
//            array[4] + other[4],
//            array[5] + other[5],
//            array[6] + other[6],
//            array[7] + other[7],
//            array[8] + other[8],
//            array[9] + other[9],
//            array[10] + other[10],
//            array[11] + other[11],
//            array[12] + other[12],
//            array[13] + other[13],
//            array[14] + other[14],
//            array[15] + other[15]
//        )
//
//    operator fun minus(other: Matrix4<T>): Matrix4<Double> =
//        Matrix4(
//            array[0] - other[0],
//            array[1] - other[1],
//            array[2] - other[2],
//            array[3] - other[3],
//            array[4] - other[4],
//            array[5] - other[5],
//            array[6] - other[6],
//            array[7] - other[7],
//            array[8] - other[8],
//            array[9] - other[9],
//            array[10] - other[10],
//            array[11] - other[11],
//            array[12] - other[12],
//            array[13] - other[13],
//            array[14] - other[14],
//            array[15] - other[15]
//        )
//
//    operator fun times(other: Matrix4<T>): Matrix4<Double> {
//        val res = Matrix4<Double>()
//        for (i in 0..3) {
//            for (j in 0..3) {
//                res[i * 4 + j] =
//                    array[i * 4] * other[j] +
//                            array[i * 4 + 1] * other[j + 4] +
//                            array[i * 4 + 2] * other[j + 8] +
//                            array[i * 4 + 3] * other[j + 12]
//            }
//        }
//        return res
//    }
//
//    operator fun times(vec: Vector4<T>): Vector4<Double> = Vector4(
//        array[0] * vec.x.toDouble() + array[1] * vec.y.toDouble() + array[2] * vec.z.toDouble() + array[3] * vec.w.toDouble(),
//        array[4] * vec.x.toDouble() + array[5] * vec.y.toDouble() + array[6] * vec.z.toDouble() + array[7] * vec.w.toDouble(),
//        array[8] * vec.x.toDouble() + array[9] * vec.y.toDouble() + array[10] * vec.z.toDouble() + array[11] * vec.w.toDouble(),
//        array[12] * vec.x.toDouble() + array[13] * vec.y.toDouble() + array[14] * vec.z.toDouble() + array[15] * vec.w.toDouble()
//    )
//
//    operator fun times(vec: Vector3<T>): Vector3<Double> = Vector3(
//        array[0] * vec.x.toDouble() + array[4] * vec.y.toDouble() + array[8] * vec.z.toDouble() + array[12],
//        array[1] * vec.x.toDouble() + array[5] * vec.y.toDouble() + array[9] * vec.z.toDouble() + array[13],
//        array[2] * vec.x.toDouble() + array[6] * vec.y.toDouble() + array[10] * vec.z.toDouble() + array[14]
//    )
//
//    fun transpose(): Matrix4<Double> =
//        Matrix4(
//            array[0], array[4], array[8], array[12],
//            array[1], array[5], array[9], array[13],
//            array[2], array[6], array[10], array[14],
//            array[3], array[7], array[11], array[15]
//        )
//
//    fun upper3x3(): Matrix3<Double> =
//        Matrix3(
//            array[0], array[1], array[2],
//            array[4], array[5], array[6],
//            array[8], array[9], array[10]
//        )
//
//    fun upper3x3Transposed(): Matrix3<Double> = upper3x3().transpose()
//
//    fun multiplyWithoutTranslation(a: Vector3<T>): Vector3<Double> {
//        return Vector3(
//            array[0] * a.x.toDouble() + array[4] * a.y.toDouble() + array[8] * a.z.toDouble(),
//            array[1] * a.x.toDouble() + array[5] * a.y.toDouble() + array[9] * a.z.toDouble(),
//            array[2] * a.x.toDouble() + array[6] * a.y.toDouble() + array[10] * a.z.toDouble()
//        )
//    }
//
//    fun inverse(): Matrix4<Double> {
//        val m = array
//        val out = DoubleArray(16)
//
//        fun mat(r: Int, c: Int) = m[c * 4 + r]
//        fun setMat(out: DoubleArray, r: Int, c: Int, value: Double) {
//            out[c * 4 + r] = value
//        }
//
//        val wtmp = Array(4) { DoubleArray(8) }
//
//        val (r0, r1, r2, r3) = wtmp
//
//        r0[0] = mat(0, 0); r0[1] = mat(0, 1); r0[2] = mat(0, 2); r0[3] = mat(0, 3)
//        r0[4] = 1.0; r0[5] = 0.0; r0[6] = 0.0; r0[7] = 0.0
//
//        r1[0] = mat(1, 0); r1[1] = mat(1, 1); r1[2] = mat(1, 2); r1[3] = mat(1, 3)
//        r1[4] = 0.0; r1[5] = 1.0; r1[6] = 0.0; r1[7] = 0.0
//
//        r2[0] = mat(2, 0); r2[1] = mat(2, 1); r2[2] = mat(2, 2); r2[3] = mat(2, 3)
//        r2[4] = 0.0; r2[5] = 0.0; r2[6] = 1.0; r2[7] = 0.0
//
//        r3[0] = mat(3, 0); r3[1] = mat(3, 1); r3[2] = mat(3, 2); r3[3] = mat(3, 3)
//        r3[4] = 0.0; r3[5] = 0.0; r3[6] = 0.0; r3[7] = 1.0
//
//        fun swapRows(a: DoubleArray, b: DoubleArray) {
//            for (i in a.indices) {
//                val tmp = a[i]
//                a[i] = b[i]
//                b[i] = tmp
//            }
//        }
//
//        if (kotlin.math.abs(r3[0]) > kotlin.math.abs(r2[0])) swapRows(r3, r2)
//        if (kotlin.math.abs(r2[0]) > kotlin.math.abs(r1[0])) swapRows(r2, r1)
//        if (kotlin.math.abs(r1[0]) > kotlin.math.abs(r0[0])) swapRows(r1, r0)
//        if (r0[0] == 0.0) return Matrix4<Double>()
//
//        for (i in 1 until 4) {
//            val m = wtmp[i][0] / r0[0]
//            for (j in 1 until 8) {
//                wtmp[i][j] -= m * r0[j]
//            }
//        }
//
//        if (kotlin.math.abs(r3[1]) > kotlin.math.abs(r2[1])) swapRows(r3, r2)
//        if (kotlin.math.abs(r2[1]) > kotlin.math.abs(r1[1])) swapRows(r2, r1)
//        if (r1[1] == 0.0) return Matrix4<Double>()
//
//        for (i in 2 until 4) {
//            val m = wtmp[i][1] / r1[1]
//            for (j in 2 until 8) {
//                wtmp[i][j] -= m * r1[j]
//            }
//        }
//
//        if (kotlin.math.abs(r3[2]) > kotlin.math.abs(r2[2])) swapRows(r3, r2)
//        if (r2[2] == 0.0) return Matrix4<Double>()
//
//        val m3 = r3[2] / r2[2]
//        for (j in 3 until 8) {
//            r3[j] -= m3 * r2[j]
//        }
//
//        if (r3[3] == 0.0) return Matrix4<Double>()
//
//        r3[4] /= r3[3]; r3[5] /= r3[3]; r3[6] /= r3[3]; r3[7] /= r3[3]
//        r2[4] -= r3[4] * r2[3]; r2[5] -= r3[5] * r2[3]
//        r2[6] -= r3[6] * r2[3]; r2[7] -= r3[7] * r2[3]
//
//        r2[4] /= r2[2]; r2[5] /= r2[2]; r2[6] /= r2[2]; r2[7] /= r2[2]
//
//        r1[4] -= r3[4] * r1[3] + r2[4] * r1[2]
//        r1[5] -= r3[5] * r1[3] + r2[5] * r1[2]
//        r1[6] -= r3[6] * r1[3] + r2[6] * r1[2]
//        r1[7] -= r3[7] * r1[3] + r2[7] * r1[2]
//
//        r1[4] /= r1[1]; r1[5] /= r1[1]; r1[6] /= r1[1]; r1[7] /= r1[1]
//
//        r0[4] -= r3[4] * r0[3] + r2[4] * r0[2] + r1[4] * r0[1]
//        r0[5] -= r3[5] * r0[3] + r2[5] * r0[2] + r1[5] * r0[1]
//        r0[6] -= r3[6] * r0[3] + r2[6] * r0[2] + r1[6] * r0[1]
//        r0[7] -= r3[7] * r0[3] + r2[7] * r0[2] + r1[7] * r0[1]
//
//        r0[4] /= r0[0]; r0[5] /= r0[0]; r0[6] /= r0[0]; r0[7] /= r0[0]
//
//        for (i in 0 until 4) {
//            for (j in 0 until 4) {
//                setMat(out, i, j, wtmp[i][j + 4])
//            }
//        }
//
//        return Matrix4<Double>().apply { array.clear(); array.addAll(out.toList()) }
//    }
//
//    fun toDoubleArray(): DoubleArray = array.toDoubleArray()
//    fun toFloatArray(): FloatArray = array.map { it.toFloat() }.toFloatArray()
//    fun toIntArray(): IntArray = array.map { it.toInt() }.toIntArray()
//
//    fun toString(fieldWidth: Int = 0, format: Char = 'g', precision: Int = -1): String =
//        "[[${array[0]}, ${array[1]}, ${array[2]}, ${array[3]}], " +
//                "[${array[4]}, ${array[5]}, ${array[6]}, ${array[7]}], " +
//                "[${array[8]}, ${array[9]}, ${array[10]}, ${array[11]}], " +
//                "[${array[12]}, ${array[13]}, ${array[14]}, ${array[15]}]]"
//
//    companion object {
//        fun identity(): Matrix4<Double> = Matrix4(
//            1.0, 0.0, 0.0, 0.0,
//            0.0, 1.0, 0.0, 0.0,
//            0.0, 0.0, 1.0, 0.0,
//            0.0, 0.0, 0.0, 1.0
//        )
//
//        fun translation(vector: Vector3<*>): Matrix4<Double> {
//            return Matrix4(
//                1, 0, 0, 0,
//                0, 1, 0, 0,
//                0, 0, 1, 0,
//                vector.x, vector.y, vector.z, 1
//            )
//        }
//
//        fun xRotation(angle: Double): Matrix4<Double> {
//            val c = cos(angle)
//            val s = sin(angle)
//
//            return Matrix4(
//                arrayOf(
//                    1.0, 0.0, 0.0, 0.0,
//                    0.0, c, s, 0.0,
//                    0.0, -s, c, 0.0,
//                    0.0, 0.0, 0.0, 1.0
//                )
//            )
//        }
//
//        fun yRotation(angle: Double): Matrix4<Double> {
//            val c = cos(angle)
//            val s = sin(angle)
//
//            return Matrix4(
//                arrayOf(
//                    c, 0.0, -s, 0.0,
//                    0.0, 1.0, 0.0, 0.0,
//                    s, 0.0, c, 0.0,
//                    0.0, 0.0, 0.0, 1.0
//                )
//            )
//        }
//
//        fun zRotation(angle: Double): Matrix4<Double> {
//            val c = cos(angle)
//            val s = sin(angle)
//
//            return Matrix4(
//                arrayOf(
//                    c, s, 0.0, 0.0,
//                    -s, c, 0.0, 0.0,
//                    0.0, 0.0, 1.0, 0.0,
//                    0.0, 0.0, 0.0, 1.0
//                )
//            )
//        }
//    }
//}