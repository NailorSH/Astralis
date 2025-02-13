//package com.nailorsh.astralis.core.utils.graphic.math.matrix
//
//import com.nailorsh.astralis.core.utils.graphic.math.vector.Vector3
//import kotlin.math.acos
//
//typealias Mat3d = Matrix3<Double>
//typealias Mat3f = Matrix3<Float>
//
//class Matrix3<T : Number> {
//    val array: MutableList<Double> = MutableList(9) { 0.0 }
//
//    constructor() {
//        setIdentity()
//    }
//
//    constructor(
//        m00: T, m01: T, m02: T,
//        m10: T, m11: T, m12: T,
//        m20: T, m21: T, m22: T
//    ) {
//        set(m00, m01, m02, m10, m11, m12, m20, m21, m22)
//    }
//
//    constructor(arr: Array<T>) {
//        require(arr.size >= 9) { "Array must contain at least 9 elements" }
//        for (i in 0 until 9) {
//            array[i] = arr[i].toDouble()
//        }
//    }
//
//    constructor(col1: Vector3<T>, col2: Vector3<T>, col3: Vector3<T>) {
//        set(
//            col1.x, col2.x, col3.x,
//            col1.y, col2.y, col3.y,
//            col1.z, col2.z, col3.z
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
//        m00: Number, m01: Number, m02: Number,
//        m10: Number, m11: Number, m12: Number,
//        m20: Number, m21: Number, m22: Number
//    ) {
//        array[0] = m00.toDouble()
//        array[1] = m01.toDouble()
//        array[2] = m02.toDouble()
//        array[3] = m10.toDouble()
//        array[4] = m11.toDouble()
//        array[5] = m12.toDouble()
//        array[6] = m20.toDouble()
//        array[7] = m21.toDouble()
//        array[8] = m22.toDouble()
//    }
//
//    fun set(
//        m00: T, m01: T, m02: T,
//        m10: T, m11: T, m12: T,
//        m20: T, m21: T, m22: T
//    ) {
//        array[0] = m00.toDouble()
//        array[1] = m01.toDouble()
//        array[2] = m02.toDouble()
//        array[3] = m10.toDouble()
//        array[4] = m11.toDouble()
//        array[5] = m12.toDouble()
//        array[6] = m20.toDouble()
//        array[7] = m21.toDouble()
//        array[8] = m22.toDouble()
//    }
//
//    fun setIdentity() {
//        set(
//            1, 0, 0,
//            0, 1, 0,
//            0, 0, 1
//        )
//    }
//
//    operator fun plus(other: Matrix3<T>): Matrix3<Double> =
//        Matrix3(
//            array[0] + other[0], array[1] + other[1], array[2] + other[2],
//            array[3] + other[3], array[4] + other[4], array[5] + other[5],
//            array[6] + other[6], array[7] + other[7], array[8] + other[8]
//        )
//
//    operator fun minus(other: Matrix3<T>): Matrix3<Double> =
//        Matrix3(
//            array[0] - other[0], array[1] - other[1], array[2] - other[2],
//            array[3] - other[3], array[4] - other[4], array[5] - other[5],
//            array[6] - other[6], array[7] - other[7], array[8] - other[8]
//        )
//
//    operator fun times(scalar: T): Matrix3<Double> =
//        Matrix3(
//            array[0] * scalar.toDouble(),
//            array[1] * scalar.toDouble(),
//            array[2] * scalar.toDouble(),
//            array[3] * scalar.toDouble(),
//            array[4] * scalar.toDouble(),
//            array[5] * scalar.toDouble(),
//            array[6] * scalar.toDouble(),
//            array[7] * scalar.toDouble(),
//            array[8] * scalar.toDouble()
//        )
//
//    operator fun div(scalar: T): Matrix3<Double> =
//        Matrix3(
//            array[0] / scalar.toDouble(),
//            array[1] / scalar.toDouble(),
//            array[2] / scalar.toDouble(),
//            array[3] / scalar.toDouble(),
//            array[4] / scalar.toDouble(),
//            array[5] / scalar.toDouble(),
//            array[6] / scalar.toDouble(),
//            array[7] / scalar.toDouble(),
//            array[8] / scalar.toDouble()
//        )
//
//    operator fun times(vec: Vector3<T>): Vector3<Double> =
//        Vector3(
//            array[0] * vec.x.toDouble() + array[1] * vec.y.toDouble() + array[2] * vec.z.toDouble(),
//            array[3] * vec.x.toDouble() + array[4] * vec.y.toDouble() + array[5] * vec.z.toDouble(),
//            array[6] * vec.x.toDouble() + array[7] * vec.y.toDouble() + array[8] * vec.z.toDouble()
//        )
//
//    operator fun times(other: Matrix3<T>): Matrix3<Double> {
//        val res = Matrix3<Double>()
//        for (i in 0..2) {
//            for (j in 0..2) {
//                res[i * 3 + j] =
//                    array[i * 3] * other[j] +
//                            array[i * 3 + 1] * other[j + 3] +
//                            array[i * 3 + 2] * other[j + 6]
//            }
//        }
//        return res
//    }
//
//    fun transpose(): Matrix3<Double> =
//        Matrix3(
//            array[0], array[3], array[6],
//            array[1], array[4], array[7],
//            array[2], array[5], array[8]
//        )
//
//    fun inverse(): Matrix3<Double> {
//        val det =
//            array[0] * (array[4] * array[8] - array[5] * array[7]) -
//                    array[1] * (array[3] * array[8] - array[5] * array[6]) +
//                    array[2] * (array[3] * array[7] - array[4] * array[6])
//
//        if (det == 0.0) throw ArithmeticException("Matrix is singular and cannot be inverted")
//
//        val invDet = 1.0 / det
//
//        return Matrix3(
//            (array[4] * array[8] - array[5] * array[7]) * invDet,
//            (array[2] * array[7] - array[1] * array[8]) * invDet,
//            (array[1] * array[5] - array[2] * array[4]) * invDet,
//
//            (array[5] * array[6] - array[3] * array[8]) * invDet,
//            (array[0] * array[8] - array[2] * array[6]) * invDet,
//            (array[2] * array[3] - array[0] * array[5]) * invDet,
//
//            (array[3] * array[7] - array[4] * array[6]) * invDet,
//            (array[1] * array[6] - array[0] * array[7]) * invDet,
//            (array[0] * array[4] - array[1] * array[3]) * invDet
//        )
//    }
//
//    fun trace(): Double = array[0] + array[4] + array[8]
//
//    fun angle(): Double = acos(0.5 * (trace() - 1.0))
//
//    fun toDoubleArray(): DoubleArray = array.toDoubleArray()
//    fun toFloatArray(): FloatArray = array.map { it.toFloat() }.toFloatArray()
//    fun toIntArray(): IntArray = array.map { it.toInt() }.toIntArray()
//
//    fun toString(fieldWidth: Int = 0, format: Char = 'g', precision: Int = -1): String =
//        "[[${array[0]}, ${array[1]}, ${array[2]}], [${array[3]}, ${array[4]}, ${array[5]}], [${array[6]}, ${array[7]}, ${array[8]}]]"
//
//    companion object {
//        fun identity(): Matrix3<Double> = Matrix3(
//            1.0, 0.0, 0.0,
//            0.0, 1.0, 0.0,
//            0.0, 0.0, 1.0
//        )
//    }
//}
