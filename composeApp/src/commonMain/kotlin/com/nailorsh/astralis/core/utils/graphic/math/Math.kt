package com.nailorsh.astralis.core.utils.graphic.math

import com.nailorsh.astralis.core.utils.graphic.math.vector.Vector3
import kotlin.math.PI

// astronomical unit (km)
const val AU = 149597870.691
const val AUf = 1.4959787E8f
const val AU_KM = 1.0 / 149597870.691
const val AU_KMf = 1.0f / 1.4959787E8f

// Parsec (km)
const val PARSEC = 30.857E12

const val PI_180 = PI / 180

fun Double.toDegrees(): Double = this * 180 / PI
fun Double.toRadians(): Double = this / 180.0 * PI

// Нечеткое сравнение двух чисел
fun fuzzyEquals(a: Double, b: Double, eps: Double = Double.MIN_VALUE): Boolean {
    if (a == b) return true
    return !((a + eps) < b || (a - eps) > b)
}

operator fun Float.times(vector: Vector3<Float>): Vector3<Float> {
    return vector.let { Vector3(it.x * this, it.y * this, it.z * this) }
}