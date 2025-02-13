//package com.nailorsh.astralis.core.utils.graphic
//
//import com.nailorsh.astralis.core.utils.Quadruple
//import kotlin.math.*
//
//const val PRECESSION_EPOCH_THRESHOLD = 1.0
//const val NUTATION_EPOCH_THRESHOLD = 1.0 / 24.0
//
//private var cPsiA = 0.0
//private var cOmegaA = 0.0
//private var cChiA = 0.0
//private var cEpsilonA = 0.0
//private var cLastJDE = -1e100
//
//private const val ARCSEC_TO_RAD = (PI * 2.0) / (360.0 * 3600.0)
//
//private val precVals = arrayOf(
//    doubleArrayOf(
//        1.0 / 402.90,
//        -22206.325946,
//        1267.727824,
//        -13765.924050,
//        -3243.236469,
//        -8571.476251,
//        -2206.967126
//    ),
//    doubleArrayOf(
//        1.0 / 256.75,
//        12236.649447,
//        1702.324248,
//        13511.858383,
//        -3969.723769,
//        5309.796459,
//        -4186.752711
//    ),
//    doubleArrayOf(
//        1.0 / 292.00,
//        -1589.008343,
//        -2970.553839,
//        -1455.229106,
//        7099.207893,
//        -610.393953,
//        6737.949677
//    ),
//    doubleArrayOf(
//        1.0 / 537.22,
//        2482.103195,
//        693.790312,
//        1054.394467,
//        -1903.696711,
//        923.201931,
//        -856.922846
//    ),
//    doubleArrayOf(1.0 / 241.45, 150.322920, -14.724451, 0.0, 146.435014, 3.759055, 0.0)
//)
//
//private val pEpsVals = arrayOf(
//    doubleArrayOf(1.0 / 409.90, -6908.287473, 753.872780, -2845.175469, -1704.720302),
//    doubleArrayOf(1.0 / 396.15, -3198.706291, -247.805823, 449.844989, -862.308358),
//    doubleArrayOf(1.0 / 537.22, 1453.674527, 379.471484, -1255.915323, 447.832178),
//    doubleArrayOf(1.0 / 402.90, -857.748557, -53.880558, 886.736783, -889.571909)
//)
//
//fun getPrecessionAnglesVondrak(jde: Double): Quadruple<Double, Double, Double, Double> {
//    if (abs(jde - cLastJDE) > PRECESSION_EPOCH_THRESHOLD) {
//        cLastJDE = jde
//        val T = (jde - 2451545.0) / 36525.0 // Юлианские века от J2000.0
//        require(abs(T) <= 2000) { "T is out of valid range!" }
//        val T2pi = T * (2.0 * PI)
//
//        var psiA = 0.0
//        var omegaA = 0.0
//        var chiA = 0.0
//        var epsilonA = 0.0
//
//        for (i in precVals.indices) {
//            val invP = precVals[i][0]
//            val phase = T2pi * invP
//            val sin2piT_P = sin(phase)
//            val cos2piT_P = cos(phase)
//
//            psiA += precVals[i][1] * cos2piT_P + precVals[i][4] * sin2piT_P
//            omegaA += precVals[i][2] * cos2piT_P + precVals[i][5] * sin2piT_P
//            chiA += precVals[i][3] * cos2piT_P + precVals[i][6] * sin2piT_P
//        }
//
//        for (i in pEpsVals.indices) {
//            val invP = pEpsVals[i][0]
//            val phase = T2pi * invP
//            val sin2piT_P = sin(phase)
//            val cos2piT_P = cos(phase)
//
//            epsilonA += pEpsVals[i][2] * cos2piT_P + pEpsVals[i][4] * sin2piT_P
//        }
//
//        psiA += ((289e-9 * T - 0.00740913) * T + 5042.7980307) * T + 8473.343527
//        omegaA += ((151e-9 * T + 0.00000146) * T - 0.4436568) * T + 84283.175915
//        chiA += ((-61e-9 * T + 0.00001472) * T + 0.0790159) * T - 19.657270
//        epsilonA += ((-110e-9 * T - 0.00004039) * T + 0.3624445) * T + 84028.206305
//
//        cPsiA = ARCSEC_TO_RAD * psiA
//        cOmegaA = ARCSEC_TO_RAD * omegaA
//        cChiA = ARCSEC_TO_RAD * chiA
//        cEpsilonA = ARCSEC_TO_RAD * epsilonA
//    }
//    return Quadruple(cPsiA, cOmegaA, cChiA, cEpsilonA)
//}
//
//fun getPrecessionAnglesVondrakPQXYe(
//    jde: Double,
//    vP_A: DoubleArray,
//    vQ_A: DoubleArray,
//    vX_A: DoubleArray,
//    vY_A: DoubleArray,
//    vepsilon_A: DoubleArray
//) {
//    if (kotlin.math.abs(jde - c_lastJDE) > PRECESSION_EPOCH_THRESHOLD) {
//        c_lastJDE = jde
//        val T = (jde - 2451545.0) / 36525.0
//        require(kotlin.math.abs(T) <= 2000) { "MAKES SURE YOU NEVER OVERSTRETCH THIS!" }
//        val T2pi = T * (2.0 * Math.PI) // Юлианские столетия от J2000.0, умноженные на 2Pi
//
//        // Переменные, соответствующие греческим буквам в статьях
//        var P_A = 0.0
//        var Q_A = 0.0
//        var X_A = 0.0
//        var Y_A = 0.0
//        var Epsilon_A = 0.0
//
//        for (i in 0 until 8) {
//            val invP = PQvals[i][0]
//            val phase = T2pi * invP
//            val sin2piT_P = kotlin.math.sin(phase)
//            val cos2piT_P = kotlin.math.cos(phase)
//
//            P_A += PQvals[i][1] * cos2piT_P + PQvals[i][3] * sin2piT_P
//            Q_A += PQvals[i][2] * cos2piT_P + PQvals[i][4] * sin2piT_P
//        }
//
//        for (i in 0 until 14) {
//            val invP = XYvals[i][0]
//            val phase = T2pi * invP
//            val sin2piT_P = kotlin.math.sin(phase)
//            val cos2piT_P = kotlin.math.cos(phase)
//
//            X_A += XYvals[i][1] * cos2piT_P + XYvals[i][3] * sin2piT_P
//            Y_A += XYvals[i][2] * cos2piT_P + XYvals[i][4] * sin2piT_P
//        }
//
//        for (i in 0 until 10) {
//            val invP = p_epsVals[i][0]
//            val phase = T2pi * invP
//            val sin2piT_P = kotlin.math.sin(phase)
//            val cos2piT_P = kotlin.math.cos(phase)
//
//            Epsilon_A += p_epsVals[i][2] * cos2piT_P + p_epsVals[i][4] * sin2piT_P
//        }
//
//        // Полиномиальные члены в T (используем схему Горнера)
//        P_A += ((110e-9 * T - 0.00028913) * T - 0.1189000) * T + 5851.607687
//        Q_A += ((-437e-9 * T - 0.00000020) * T + 1.1689818) * T - 1600.886300
//        X_A += ((-152e-9 * T - 0.00037173) * T + 0.4252841) * T + 5453.282155
//        Y_A += ((231e-9 * T - 0.00018725) * T - 0.7675452) * T - 73750.930350
//        Epsilon_A += ((110e-9 * T - 0.00004039) * T + 0.3624445) * T + 84028.206305
//
//        c_P_A = arcSec2Rad * P_A
//        c_Q_A = arcSec2Rad * Q_A
//        c_X_A = arcSec2Rad * X_A
//        c_Y_A = arcSec2Rad * Y_A
//        c_epsilon_A = arcSec2Rad * Epsilon_A
//    }
//
//    vP_A[0] = c_P_A
//    vQ_A[0] = c_Q_A
//    vX_A[0] = c_X_A
//    vY_A[0] = c_Y_A
//    vepsilon_A[0] = c_epsilon_A
//}
//
//// Функция, возвращающая наклон эклиптики (предположительно предвычисленный)
//fun getPrecessionAngleVondrakEpsilon(jde: Double): Double {
//    val epsilon_A = DoubleArray(1)
//    val dummyChi_A = DoubleArray(1)
//    val dummyOmega_A = DoubleArray(1)
//    val dummyPsi_A = DoubleArray(1)
//    getPrecessionAnglesVondrak(jde, epsilon_A, dummyChi_A, dummyOmega_A, dummyPsi_A)
//    return epsilon_A[0]
//}
//
//// Возвращает текущий наклон эклиптики (предположительно предвычисленный)
//fun getPrecessionAngleVondrakCurrentEpsilonA(): Double {
//    return c_epsilon_A
//}
