//package com.nailorsh.astralis.core.utils.graphics.math.vector
//
//import kotlin.math.pow
//import kotlin.math.sqrt
//
//class Vector4<T : Number>(
//    var x: T,
//    var y: T,
//    var z: T,
//    var w: T
//) {
//    constructor(value: T) : this(value, value, value, value)
//
//    constructor(arr: Array<T>) : this(arr[0], arr[1], arr[2], arr[3])
//
//    constructor(vec3: Vector3<T>, w: T) : this(vec3.x, vec3.y, vec3.z, w)
//
//    @Suppress("UNCHECKED_CAST")
//    constructor(vec3: Vector3<T>) : this(vec3.x, vec3.y, vec3.z, 1 as T)
//
//    fun set(x: T, y: T, z: T, w: T) {
//        this.x = x
//        this.y = y
//        this.z = z
//        this.w = w
//    }
//
//    operator fun plus(other: Vector4<T>): Vector4<Double> = Vector4(
//        x.toDouble() + other.x.toDouble(),
//        y.toDouble() + other.y.toDouble(),
//        z.toDouble() + other.z.toDouble(),
//        w.toDouble() + other.w.toDouble()
//    )
//
//    operator fun minus(other: Vector4<T>): Vector4<Double> = Vector4(
//        x.toDouble() - other.x.toDouble(),
//        y.toDouble() - other.y.toDouble(),
//        z.toDouble() - other.z.toDouble(),
//        w.toDouble() - other.w.toDouble()
//    )
//
//    operator fun times(scalar: T): Vector4<Double> = Vector4(
//        x.toDouble() * scalar.toDouble(),
//        y.toDouble() * scalar.toDouble(),
//        z.toDouble() * scalar.toDouble(),
//        w.toDouble() * scalar.toDouble()
//    )
//
//    operator fun div(scalar: T): Vector4<Double> = Vector4(
//        x.toDouble() / scalar.toDouble(),
//        y.toDouble() / scalar.toDouble(),
//        z.toDouble() / scalar.toDouble(),
//        w.toDouble() / scalar.toDouble()
//    )
//
//    infix fun dot(other: Vector4<T>): Double =
//        x.toDouble() * other.x.toDouble() +
//                y.toDouble() * other.y.toDouble() +
//                z.toDouble() * other.z.toDouble() +
//                w.toDouble() * other.w.toDouble()
//
//    fun norm(): Double = sqrt(normSquared())
//
//    fun normSquared(): Double =
//        x.toDouble().pow(2) + y.toDouble().pow(2) + z.toDouble().pow(2) + w.toDouble().pow(2)
//
//    fun normalize(): Vector4<Double> {
//        val length = norm()
//        return if (length == 0.0) Vector4(0.0, 0.0, 0.0, 0.0)
//        else Vector4(
//            x.toDouble() / length,
//            y.toDouble() / length,
//            z.toDouble() / length,
//            w.toDouble() / length
//        )
//    }
//
//    fun toDoubleArray(): DoubleArray = doubleArrayOf(
//        x.toDouble(),
//        y.toDouble(),
//        z.toDouble(),
//        w.toDouble()
//    )
//
//    fun toFloatArray(): FloatArray = floatArrayOf(
//        x.toFloat(),
//        y.toFloat(),
//        z.toFloat(),
//        w.toFloat()
//    )
//
//    fun toIntArray(): IntArray = intArrayOf(
//        x.toInt(),
//        y.toInt(),
//        z.toInt(),
//        w.toInt()
//    )
//
//    override fun toString(): String = "[$x, $y, $z, $w]"
//}
