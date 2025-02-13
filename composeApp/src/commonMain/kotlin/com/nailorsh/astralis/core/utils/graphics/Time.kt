//package com.nailorsh.astralis.core.utils.graphic
//
//import kotlin.math.*
//
///* puts a large angle in the correct range 0 - 360 degrees */
//fun rangeDegrees(d: Double): Double {
//    var d = d % 360.0
//    if (d < 0.0) d += 360.0
//    return d
//}
//
///* puts a large angle in the correct range 0 - 2PI radians */
//fun rangeRadians(r: Double): Double {
//    var r = r % (2 * PI)
//    if (r < 0.0) r += 2 * PI
//    return r
//}
//
///* Calculate the mean sidereal time at the meridian of Greenwich (GMST) of a given date.
// * returns mean sidereal time (degrees).
// * Meeus, Astr. Algorithms, Formula 11.1, 11.4 pg 83. (or 2nd ed. 1998, 12.1, 12.4 pg.87)
// * MAKE SURE argument JD is UT, not TT!
// */
//fun getMeanSiderealTime(JD: Double, JDE: Double): Double {
//    val UT1 = (JD - floor(JD) + 0.5) * 86400.0  // time in seconds
//    val t = (JDE - 2451545.0) / 36525.0
//    val tu = (JD - 2451545.0) / 36525.0
//
//    var sidereal =
//        (((-0.000000002454 * t - 0.00000199708) * t - 0.0000002926) * t + 0.092772110) * t * t
//    sidereal += (t - tu) * 307.4771013
//    sidereal += 8640184.79447825 * tu + 24110.5493771
//    sidereal += UT1
//
//    // this is expressed in seconds. We need degrees.
//    // 1deg = 4 tempMin = 240 tempSec
//    sidereal *= 1.0 / 240.0
//
//    /* add again a convenient multiple of 360 degrees */
//    sidereal = rangeDegrees(sidereal)
//
//    return sidereal
//}
//
///* Calculate the apparent sidereal time at the meridian of Greenwich of a given date.
// * returns apparent sidereal time (degrees).
// * Formula 11.1, 11.4 pg 83
// */
//fun getApparentSiderealTime(JD: Double, JDE: Double): Double {
//    val meanSidereal = getMeanSiderealTime(JD, JDE)
//
//    // add corrections for nutation in longitude and for the true obliquity of the ecliptic
//    val (deltaPsi, deltaEps) = getNutationAngles(JDE)
//
//    return meanSidereal + (deltaPsi * cos(getPrecessionAngleVondrakEpsilon(JDE) + deltaEps)) * 180.0 / PI
//}
