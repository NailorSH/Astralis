package com.nailorsh.astralis.core.utils.graphics.math

import com.nailorsh.astralis.core.utils.graphics.math.matrix.Mat4d
import com.nailorsh.astralis.core.utils.graphics.math.matrix.Mat4f
import com.nailorsh.astralis.core.utils.graphics.math.vector.Vector3
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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

operator fun Double.times(vector: Vector3<Double>): Vector3<Double> {
    return vector.let { Vector3(it.x * this, it.y * this, it.z * this) }
}


fun Mat4d.toMat4f(): Mat4f = Mat4f(array.map { it.toFloat() }.toTypedArray())
fun Mat4f.toMat4d(): Mat4d = Mat4d(array.toTypedArray())

private const val MAX_STACKS = 4096
private const val MAX_SLICES = 4096

/**
 * Вычисляет косинусы и синусы вокруг окружности, разделенной на `slices` частей.
 * Используется для значений sin/cos вдоль широтного круга, экватора и т. д. для сферической сетки.
 * @param slices Количество разбиений (иначе называемых "segments") для окружности.
 * @return Массив cos/sin значений.
 */
fun computeCosSinTheta(slices: Int): FloatArray {
    require(slices <= MAX_SLICES) { "slices must be <= $MAX_SLICES" }

    val dTheta = (2 * PI).toFloat() / slices.toFloat()
    val c = cos(dTheta)
    val s = sin(dTheta)

    val cosSin = FloatArray(2 * (slices + 1))

    var forwardIndex = 0
    var reverseIndex = 2 * slices

    cosSin[forwardIndex++] = 1f
    cosSin[forwardIndex++] = 0f
    cosSin[reverseIndex--] = -cosSin[forwardIndex - 1]
    cosSin[reverseIndex--] = cosSin[forwardIndex - 2]

    cosSin[forwardIndex++] = c
    cosSin[forwardIndex++] = s
    cosSin[reverseIndex--] = -cosSin[forwardIndex - 1]
    cosSin[reverseIndex--] = cosSin[forwardIndex - 2]

    while (forwardIndex < reverseIndex) {
        cosSin[forwardIndex] = cosSin[forwardIndex - 2] * c - cosSin[forwardIndex - 1] * s
        cosSin[forwardIndex + 1] = cosSin[forwardIndex - 2] * s + cosSin[forwardIndex - 1] * c
        forwardIndex += 2
        cosSin[reverseIndex--] = -cosSin[forwardIndex - 1]
        cosSin[reverseIndex--] = cosSin[forwardIndex - 2]
    }

    return cosSin
}

fun computeCosSinRho(segments: Int): FloatArray {
    require(segments <= MAX_STACKS) { "segments must be <= $MAX_STACKS" }

    val dRho = PI.toFloat() / segments.toFloat()
    val c = cos(dRho)
    val s = sin(dRho)

    val cosSin = FloatArray(2 * (segments + 1))

    var forwardIndex = 0
    var reverseIndex = 2 * segments

    cosSin[forwardIndex++] = 1f
    cosSin[forwardIndex++] = 0f
    cosSin[reverseIndex--] = cosSin[forwardIndex - 1]
    cosSin[reverseIndex--] = -cosSin[forwardIndex - 2]

    cosSin[forwardIndex++] = c
    cosSin[forwardIndex++] = s
    cosSin[reverseIndex--] = cosSin[forwardIndex - 1]
    cosSin[reverseIndex--] = -cosSin[forwardIndex - 2]

    while (forwardIndex < reverseIndex) {
        cosSin[forwardIndex] = cosSin[forwardIndex - 2] * c - cosSin[forwardIndex - 1] * s
        cosSin[forwardIndex + 1] = cosSin[forwardIndex - 2] * s + cosSin[forwardIndex - 1] * c
        forwardIndex += 2
        cosSin[reverseIndex--] = cosSin[forwardIndex - 1]
        cosSin[reverseIndex--] = -cosSin[forwardIndex - 2]
    }

    return cosSin
}