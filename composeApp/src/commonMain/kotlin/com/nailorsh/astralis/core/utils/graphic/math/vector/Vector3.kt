package com.nailorsh.astralis.core.utils.graphic.math.vector

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
    constructor(value: T) : this(value, value, value)

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

    fun toDoubleArray(): DoubleArray = doubleArrayOf(x.toDouble(), y.toDouble(), z.toDouble())
    fun toFloatArray(): FloatArray = floatArrayOf(x.toFloat(), y.toFloat(), z.toFloat())
    fun toIntArray(): IntArray = intArrayOf(x.toInt(), y.toInt(), z.toInt())

    override fun toString(): String = "[$x, $y, $z]"
}
