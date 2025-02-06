package com.nailorsh.astralis.core.utils.graphic.math

import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

typealias Vec3d = Vector3<Double>
typealias Vec3f = Vector3<Float>

class Vector3<T : Number>(var x: T, var y: T, var z: T) {
    constructor(value: T) : this(value, value, value)
    constructor(array: Array<T>) : this(array[0], array[1], array[2])

    fun set(x: T, y: T, z: T) {
        this.x = x
        this.y = y
        this.z = z
    }

    override fun equals(other: Any?): Boolean {
        return other is Vector3<*> && other.x == x && other.y == y && other.z == z
    }

    override fun hashCode() = 31 * (31 * x.hashCode() + y.hashCode()) + z.hashCode()

    fun norm(): Double {
        return sqrt(x.toDouble().pow(2) + y.toDouble().pow(2) + z.toDouble().pow(2))
    }

    fun normSquared(): Double {
        return x.toDouble().pow(2) + y.toDouble().pow(2) + z.toDouble().pow(2)
    }

    @Suppress("UNCHECKED_CAST")
    fun normalize() {
        val length = norm()
        if (length != 0.0) {
            x = (x.toDouble() / length) as T
            y = (y.toDouble() / length) as T
            z = (z.toDouble() / length) as T
        }
    }

    fun dot(other: Vector3<T>): Double {
        return x.toDouble() * other.x.toDouble() +
                y.toDouble() * other.y.toDouble() +
                z.toDouble() * other.z.toDouble()
    }

    fun cross(other: Vector3<T>): Vector3<Double> {
        return Vector3(
            y.toDouble() * other.z.toDouble() - z.toDouble() * other.y.toDouble(),
            z.toDouble() * other.x.toDouble() - x.toDouble() * other.z.toDouble(),
            x.toDouble() * other.y.toDouble() - y.toDouble() * other.x.toDouble()
        )
    }

    fun angle(other: Vector3<T>): Double {
        val dotProduct = dot(other)
        val norms = norm() * other.norm()
        return if (norms == 0.0) 0.0 else acos(dotProduct / norms)
    }

    operator fun plus(other: Vector3<T>): Vector3<Double> {
        return Vector3(
            x.toDouble() + other.x.toDouble(),
            y.toDouble() + other.y.toDouble(),
            z.toDouble() + other.z.toDouble()
        )
    }

    operator fun minus(other: Vector3<T>): Vector3<Double> {
        return Vector3(
            x.toDouble() - other.x.toDouble(),
            y.toDouble() - other.y.toDouble(),
            z.toDouble() - other.z.toDouble()
        )
    }

    operator fun times(scalar: Double): Vector3<Double> {
        return Vector3(
            x.toDouble() * scalar,
            y.toDouble() * scalar,
            z.toDouble() * scalar
        )
    }

    operator fun div(scalar: Double): Vector3<Double> {
        return Vector3(
            x.toDouble() / scalar,
            y.toDouble() / scalar,
            z.toDouble() / scalar
        )
    }

    override fun toString(): String {
        return "[$x, $y, $z]"
    }
}
