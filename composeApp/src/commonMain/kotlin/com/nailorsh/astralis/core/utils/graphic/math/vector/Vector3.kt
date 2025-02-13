package com.nailorsh.astralis.core.utils.graphic.math.vector

import com.nailorsh.astralis.core.utils.graphic.math.matrix.Mat4d
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

typealias Vec3d = Vector3<Double>
typealias Vec3f = Vector3<Float>
typealias Vec3i = Vector3<Int>

class Vector3<T : Number>(
    var x: T,
    var y: T,
    var z: T
) {
    @Suppress("UNCHECKED_CAST")
    constructor(value: T = 0 as T) : this(value, value, value)

    constructor(arr: Array<T>) : this(arr[0], arr[1], arr[2])

    fun set(x: T, y: T, z: T) {
        this.x = x
        this.y = y
        this.z = z
    }

    operator fun plus(other: Vector3<T>): Vector3<Double> = Vector3(
        x.toDouble() + other.x.toDouble(),
        y.toDouble() + other.y.toDouble(),
        z.toDouble() + other.z.toDouble()
    )

    operator fun minus(other: Vector3<T>): Vector3<Double> = Vector3(
        x.toDouble() - other.x.toDouble(),
        y.toDouble() - other.y.toDouble(),
        z.toDouble() - other.z.toDouble()
    )

    operator fun times(scalar: T): Vector3<Double> = Vector3(
        x.toDouble() * scalar.toDouble(),
        y.toDouble() * scalar.toDouble(),
        z.toDouble() * scalar.toDouble()
    )

    operator fun times(vector: Vector3<T>): Double {
        val x1 = x.toDouble()
        val y1 = y.toDouble()
        val z1 = z.toDouble()
        val x2 = vector.x.toDouble()
        val y2 = vector.y.toDouble()
        val z2 = vector.z.toDouble()

        return x1 * x2 + y1 * y2 + z1 * z2
    }


    private fun toNumberType(value: T) {
        when (value) {
            is Double -> value.toDouble()
            is Float -> value.toFloat()
            is Int -> value.toInt()
        }
    }

    operator fun div(scalar: T): Vector3<Double> = Vector3(
        x.toDouble() / scalar.toDouble(),
        y.toDouble() / scalar.toDouble(),
        z.toDouble() / scalar.toDouble()
    )

    infix fun dot(other: Vector3<T>): Double {
        return x.toDouble() * other.x.toDouble() + y.toDouble() * other.y.toDouble() + z.toDouble() * other.z.toDouble()
    }

    infix fun cross(other: Vector3<T>): Vector3<Double> =
        Vector3(
            y.toDouble() * other.z.toDouble() - z.toDouble() * other.y.toDouble(),
            z.toDouble() * other.x.toDouble() - x.toDouble() * other.z.toDouble(),
            x.toDouble() * other.y.toDouble() - y.toDouble() * other.x.toDouble()
        )

    fun norm(): Double = sqrt(x.toDouble().pow(2) + y.toDouble().pow(2) + z.toDouble().pow(2))

    fun normSquared(): Double = x.toDouble().pow(2) + y.toDouble().pow(2) + z.toDouble().pow(2)

    fun normalize(): Vector3<Double> {
        val length = norm()
        return if (length == 0.0) Vector3(0.0, 0.0, 0.0)
        else Vector3(x.toDouble() / length, y.toDouble() / length, z.toDouble() / length)
    }

    fun toStringLonLat(): String =
        "[${longitude() * 180.0 / PI}, ${latitude() * 180.0 / PI}]"

    fun latitude(): Double = asin(z.toDouble() / norm())

    fun longitude(): Double = atan2(y.toDouble(), x.toDouble())

//    template<class T> void Vector3<T>::transfo4d(const Mat4d& m)
//    {
//        const T v0 = v[0];
//        const T v1 = v[1];
//        v[0]=m.r[0]*v0 + m.r[4]*v1 + m.r[8]*v[2] + m.r[12];
//        v[1]=m.r[1]*v0 + m.r[5]*v1 +  m.r[9]*v[2] + m.r[13];
//        v[2]=m.r[2]*v0 + m.r[6]*v1 + m.r[10]*v[2] + m.r[14];
//    }

    @Suppress("UNCHECKED_CAST")
    fun transfo4d(matrix: Mat4d) {
        val tempX = x.toDouble()
        val tempY = y.toDouble()
        val tempZ = z.toDouble()

        x =
            (matrix.array[0] * tempX + matrix.array[4] * tempY + matrix.array[8] * tempZ + matrix.array[12]) as T
        y =
            (matrix.array[1] * tempX + matrix.array[5] * tempY + matrix.array[9] * tempZ + matrix.array[13]) as T
        z =
            (matrix.array[2] * tempX + matrix.array[6] * tempY + matrix.array[10] * tempZ + matrix.array[14]) as T
    }


    fun toDoubleArray(): DoubleArray = doubleArrayOf(x.toDouble(), y.toDouble(), z.toDouble())
    fun toFloatArray(): FloatArray = floatArrayOf(x.toFloat(), y.toFloat(), z.toFloat())
    fun toIntArray(): IntArray = intArrayOf(x.toInt(), y.toInt(), z.toInt())

    fun toVec3d(): Vec3d = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())
    fun toVec3f(): Vec3f = Vec3f(x.toFloat(), y.toFloat(), z.toFloat())

    override fun toString(): String = "[$x, $y, $z]"
}
