package com.nailorsh.astralis.core.utils.time

import kotlin.math.floor

fun getDateFromJulianDay(jd: Double): Triple<Int, Int, Int> {
    /*
     * Этот алгоритм взят из
     * "Numerical Recipes in C, 2nd Ed." (1992), стр. 14-15
     * и конвертирован в целочисленную математику.
     * Электронная версия книги доступна бесплатно на
     * http://www.nr.com/ в разделе "Obsolete Versions".
     */

    val JD_GREG_CAL = 2299161L
    val JB_MAX_WITHOUT_OVERFLOW = 107374182L
    val julian = floor(jd + 0.5).toLong()

    val ta: Long
    val jalpha: Long
    val tb: Long
    val tc: Long
    val td: Long
    val te: Long

    if (julian >= JD_GREG_CAL) {
        jalpha = (4 * (julian - 1867216) - 1) / 146097
        ta = julian + 1 + jalpha - jalpha / 4
    } else if (julian < 0) {
        ta = julian + 36525 * (1 - julian / 36525)
    } else {
        ta = julian
    }

    tb = ta + 1524
    tc = if (tb <= JB_MAX_WITHOUT_OVERFLOW) {
        (tb * 20 - 2442) / 7305
    } else {
        ((tb.toULong() * 20u - 2442u) / 7305u).toLong()
    }
    td = 365 * tc + tc / 4
    te = ((tb - td) * 10000) / 306001

    val day = (tb - td - (306001 * te) / 10000).toInt()
    var month = (te - 1).toInt()
    if (month > 12) {
        month -= 12
    }
    var year = (tc - 4715).toInt()
    if (month > 2) {
        year--
    }
    if (julian < 0) {
        year -= 100 * (1 - (julian / 36525).toInt())
    }

    return Triple(year, month, day)
}

// Определяет номер дня в году для данной даты
fun dayInYear(year: Int, month: Int, day: Int): Int {
    val k = if (isLeapYear(year)) 1 else 2
    return (275 * month / 9) - k * ((month + 9) / 12) + day - 30
}

// Проверяет, является ли год високосным, учитывая переход с юлианского на григорианский календарь в 1582 году
fun isLeapYear(year: Int): Boolean {
    return if (year > 1582) {
        if (year % 100 == 0) year % 400 == 0 else year % 4 == 0
    } else {
        year % 4 == 0
    }
}


// Возвращает дробный год в формате yyyy.ddddd. Для отрицательных годов номер года,
// конечно, уменьшается. Например, -500.5 встречается в -501.
fun yearFraction(year: Int, month: Int, day: Int): Double {
    val d = dayInYear(year, month, 0) + day
    val daysInYear = if (isLeapYear(year)) 366.0 else 365.0
    return year + d / daysInYear
}

fun getMoonSecularAcceleration(jDay: Double, nd: Double, useDE4xx: Boolean): Double {
    val (year, month, day) = getDateFromJulianDay(jDay)
    val t = (yearFraction(year, month, day) - 1955.5) / 100.0
    val ephND = if (useDE4xx) -25.8 else -23.8946
    return -0.91072 * (ephND + kotlin.math.abs(nd)) * t * t
}

fun getDeltaTByEspenakMeeus(jDay: Double): Double {
    val (year, month, day) = getDateFromJulianDay(jDay)
    val y = yearFraction(year, month, day)

    var u = (y - 1820) / 100.0
    var r = -20 + 32 * u * u

    when {
        y < -500 -> {} // Значения уже установлены по умолчанию
        y < 500 -> {
            u = y / 100.0
            r =
                (((((0.0090316521 * u + 0.022174192) * u - 0.1798452) * u - 5.952053) * u + 33.78311) * u - 1014.41) * u + 10583.6
        }

        y < 1600 -> {
            u = (y - 1000) / 100.0
            r =
                (((((0.0083572073 * u - 0.005050998) * u - 0.8503463) * u + 0.319781) * u + 71.23472) * u - 556.01) * u + 1574.2
        }

        y < 1700 -> {
            val t = y - 1600
            r = ((t / 7129.0 - 0.01532) * t - 0.9808) * t + 120.0
        }

        y < 1800 -> {
            val t = y - 1700
            r = (((-t / 1174000.0 + 0.00013336) * t - 0.0059285) * t + 0.1603) * t + 8.83
        }

        y < 1860 -> {
            val t = y - 1800
            r =
                ((((((0.000000000875 * t - 0.0000001699) * t + 0.0000121272) * t - 0.00037436) * t + 0.0041116) * t + 0.0068612) * t - 0.332447) * t + 13.72
        }

        y < 1900 -> {
            val t = y - 1860
            r =
                ((((t / 233174.0 - 0.0004473624) * t + 0.01680668) * t - 0.251754) * t + 0.5737) * t + 7.62
        }

        y < 1920 -> {
            val t = y - 1900
            r = (((-0.000197 * t + 0.0061966) * t - 0.0598939) * t + 1.494119) * t - 2.79
        }

        y < 1941 -> {
            val t = y - 1920
            r = ((0.0020936 * t - 0.076100) * t + 0.84493) * t + 21.20
        }

        y < 1961 -> {
            val t = y - 1950
            r = ((t / 2547.0 - 1.0 / 233.0) * t + 0.407) * t + 29.07
        }

        y < 1986 -> {
            val t = y - 1975
            r = ((-t / 718.0 - 1 / 260.0) * t + 1.067) * t + 45.45
        }

        y < 2005 -> {
            val t = y - 2000
            r =
                ((((0.00002373599 * t + 0.000651814) * t + 0.0017275) * t - 0.060374) * t + 0.3345) * t + 63.86
        }

        y < 2015 -> {
            val t = y - 2005
            r = 0.2930 * t + 64.69
        }

        y < 3000 -> {
            val t = y - 2015
            r = (0.0039755 * t + 0.3645) * t + 67.62
        }

        y < 3100 -> {
            r = 4283.78 + (y - 3000) * 9.391
        }
    }

    return r
}
