package com.nailorsh.astralis

import com.nailorsh.astralis.core.utils.graphic.math.vector.Vec3d

// Базовый абстрактный класс для небесных объектов, используемых в Stellarium
abstract class CelestialObject {
    abstract fun getJ2000EquatorialPos(core: AstralisCore): Vec3d

    open fun getAltAzPosAuto(core: AstralisCore): Vec3d = getJ2000EquatorialPos(core)

    abstract suspend fun getVMagnitude(core: AstralisCore): Float

    open suspend fun getVMagnitudeWithExtinction(
        core: AstralisCore,
        knownVMag: Float = -1000f,
        magOffset: Float = 0f
    ): Float {
        return getVMagnitude(core)
    }

    fun getEquinoxEquatorialPos(core: AstralisCore): Vec3d {
        return core.j2000ToEquinoxEqu(getJ2000EquatorialPos(core), AstralisCore.RefractionMode.OFF);
    }
}
