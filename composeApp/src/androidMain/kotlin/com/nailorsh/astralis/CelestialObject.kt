//package com.nailorsh.astralis
//
//import com.nailorsh.astralis.core.utils.graphics.math.vector.Vec3d
//
//// Базовый абстрактный класс для небесных объектов, используемых в Stellarium
//abstract class CelestialObject {
//    abstract fun getJ2000EquatorialPos(core: AstralisCore): Vec3d
//
//    open fun getAltAzPosAuto(core: AstralisCore): Vec3d = getJ2000EquatorialPos(core)
//
//    fun getEquinoxEquatorialPos(core: AstralisCore): Vec3d {
//        return core.j2000ToEquinoxEqu(getJ2000EquatorialPos(core), AstralisCore.RefractionMode.OFF);
//    }
//}
