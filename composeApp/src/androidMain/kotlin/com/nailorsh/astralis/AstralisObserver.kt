//package com.nailorsh.astralis
//
//import com.nailorsh.astralis.core.utils.graphic.math.matrix.Mat4d
//import kotlin.math.PI
//
//abstract class AstralisObserver(loc: AstralisLocation) {
//    private val ssystem: SolarSystem = SolarSystem.getInstance()
//
//    private var currentLocation: AstralisLocation = loc
//    private var planet: Planet = ssystem.searchByEnglishName(loc.planetName) ?: ssystem.getEarth()
//
//    // Для Земли требуется JD, для других планет — JDE для описания вращения!
//    fun getRotAltAzToEquatorial(JD: Double, JDE: Double): Mat4d {
//        val lat = currentLocation.latitude.toDouble().coerceIn(-90.0, 90.0)
//
//        return Mat4d.zRotation(
//            (planet.getSiderealTime(JD, JDE) + currentLocation.longitude) *
//                    Math.PI / 180.0
//        ) * Mat4d.yRotation((90.0 - lat) * PI / 180.0)
//    }
//
//}