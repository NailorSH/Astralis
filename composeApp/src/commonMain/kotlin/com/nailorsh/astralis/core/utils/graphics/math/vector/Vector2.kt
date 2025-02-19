//package com.nailorsh.astralis.core.utils.graphics.math.vector
//
//import kotlin.math.max
//import kotlin.math.min
//import kotlin.math.pow
//import kotlin.math.sqrt
//
//typealias Vec2d = Vector2<Double>
//typealias Vec2f = Vector2<Float>
//typealias Vec2i = Vector2<Int>
//
//class Vector2<T : Number>(
//    var x: T,
//    var y: T
//) {
//    constructor(value: T) : this(value, value)
//
//    constructor(arr: Array<T>) : this(arr[0], arr[1])
//
//    fun set(x: T, y: T) {
//        this.x = x
//        this.y = y
//    }
//
//    operator fun plus(other: Vector2<T>): Vector2<Double> = Vector2(
//        x.toDouble() + other.x.toDouble(),
//        y.toDouble() + other.y.toDouble()
//    )
//
//    operator fun minus(other: Vector2<T>): Vector2<Double> = Vector2(
//        x.toDouble() - other.x.toDouble(),
//        y.toDouble() - other.y.toDouble()
//    )
//
//    operator fun times(scalar: T): Vector2<Double> = Vector2(
//        x.toDouble() * scalar.toDouble(),
//        y.toDouble() * scalar.toDouble()
//    )
//
//    operator fun times(other: Vector2<T>): Vector2<Double> = Vector2(
//        x.toDouble() * other.x.toDouble(),
//        y.toDouble() * other.y.toDouble()
//    )
//
//    operator fun div(scalar: T): Vector2<Double> = Vector2(
//        x.toDouble() / scalar.toDouble(),
//        y.toDouble() / scalar.toDouble()
//    )
//
//    operator fun div(other: Vector2<T>): Vector2<Double> = Vector2(
//        x.toDouble() / other.x.toDouble(),
//        y.toDouble() / other.y.toDouble()
//    )
//
//    infix fun dot(other: Vector2<T>): Double =
//        x.toDouble() * other.x.toDouble() + y.toDouble() * other.y.toDouble()
//
//    fun min(other: Vector2<T>): Vector2<Double> = Vector2(
//        min(x.toDouble(), other.x.toDouble()),
//        min(y.toDouble(), other.y.toDouble())
//    )
//
//    fun max(other: Vector2<T>): Vector2<Double> = Vector2(
//        max(x.toDouble(), other.x.toDouble()),
//        max(y.toDouble(), other.y.toDouble())
//    )
//
//    fun clamp(low: Vector2<T>, high: Vector2<T>): Vector2<Double> = Vector2(
//        x.toDouble().coerceIn(low.x.toDouble(), high.x.toDouble()),
//        y.toDouble().coerceIn(low.y.toDouble(), high.y.toDouble())
//    )
//
//    fun norm(): Double = sqrt(x.toDouble().pow(2) + y.toDouble().pow(2))
//
//    fun normSquared(): Double = x.toDouble().pow(2) + y.toDouble().pow(2)
//
//    fun normalize(): Vector2<Double> {
//        val length = norm()
//        return if (length == 0.0) Vector2(0.0, 0.0)
//        else Vector2(x.toDouble() / length, y.toDouble() / length)
//    }
//
//    fun toDoubleArray(): DoubleArray = doubleArrayOf(x.toDouble(), y.toDouble())
//    fun toFloatArray(): FloatArray = floatArrayOf(x.toFloat(), y.toFloat())
//    fun toIntArray(): IntArray = intArrayOf(x.toInt(), y.toInt())
//
//    override fun toString(): String = "[$x, $y]"
//}
