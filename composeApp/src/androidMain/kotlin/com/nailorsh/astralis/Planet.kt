package com.nailorsh.astralis

import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import com.nailorsh.astralis.core.utils.graphic.getApparentSiderealTime
import com.nailorsh.astralis.core.utils.graphic.getMeanSiderealTime
import com.nailorsh.astralis.core.utils.graphic.math.AU
import com.nailorsh.astralis.core.utils.graphic.math.PARSEC
import com.nailorsh.astralis.core.utils.graphic.math.fuzzyEquals
import com.nailorsh.astralis.core.utils.graphic.math.matrix.Mat4d
import com.nailorsh.astralis.core.utils.graphic.math.times
import com.nailorsh.astralis.core.utils.graphic.math.vector.Vec3d
import com.nailorsh.astralis.core.utils.graphic.math.vector.Vec3f
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

const val J2000 = 2451545.0
const val ORBIT_SEGMENTS = 360

// The callback type for the external position computation function
// arguments are JDE, position[3], velocity[3].
// The last variable is the userData pointer, which is Q_NULLPTR for Planets, but used in derived classes. E.g. points to the KeplerOrbit for Comets.

class Planet(
    private val name: String,
    private val equatorialRadius: Double,
    private val haloColor: Vec3f,
    private val hidden: Boolean,
    private val eclipticPos: Vec3d,
    private val aberrationPush: Vec3d,
    private val rotLocalToParent: Mat4d,
    private val pType: PlanetType,
    private val rings: Boolean,
    private val coordFunc: (
        jde: Double,
        position: DoubleArray,
        velocity: DoubleArray,
        userData: Any?
    ) -> Unit,
    private val flagLabels: Boolean,
) : CelestialObject() {
    private var lastJDE: Double = J2000
    private var orbitPositionsCache = InMemoryKache<Double, Vec3d>((ORBIT_SEGMENTS * 2).toLong()) {
        strategy = KacheStrategy.LRU
    }
    private val parent: Planet? = null
    private val rotationElements = RotationElements()  // Rotation and axis orientation parameters
    private val sphereScale = 1.0  // Rotation and axis orientation parameters

    enum class ApparentMagnitudeAlgorithm {
        MUELLER_1893,               // G. Mueller, based on visual observations 1877-91. [Explanatory Supplement to the Astronomical Almanac, 1961]
        ASTRONOMICAL_ALMANAC_1984,   // Astronomical Almanac 1984 and later. These give V (instrumental) magnitudes (allegedly from D.L. Harris, but this is wrong!)
        EXPLANATORY_SUPPLEMENT_1992, // Algorithm provided by Pere Planesas (Observatorio Astronomico Nacional) (Was called "Planesas")
        EXPLANATORY_SUPPLEMENT_2013, // Explanatory Supplement to the Astronomical Almanac, 3rd edition 2013
        MALLAMA_HILTON_2018,         // A. Mallama, J. L. Hilton: Computing apparent planetary magnitudes for the Astronomical Almanac. Astron.&Computing 25 (2018) 10-24
        UNDEFINED_ALGORITHM,
        GENERIC                     // Visual magnitude based on phase angle and albedo. The formula source for this is totally unknown!
    };


    suspend fun draw(
        core: AstralisCore,
        eclipseFactor: Double
    ) {
        if (hidden) return

        val ss = SolarSystem.getInstance()
        val vMagnitude = getVMagnitude(core, eclipseFactor)

        // Исключаем отрисовку, если превышен лимит по яркости
        if (core.skyDrawer.planetMagnitudeLimitEnabled &&
            vMagnitude > core.skyDrawer.customPlanetMagnitudeLimit.toFloat()
        ) {
            if (eclipseFactor == 1.0) return
        }

        val isSun = this == ss.sun
        val currentLocationIsEarth = core.currentLocation.planetName == "Earth"

        if (isSun && currentLocationIsEarth) {
            val lmgr = LandscapeMgr.getInstance()
            val posAltAz = getAltAzPosAuto(core).toVec3f().normalize()

            if (posAltAz.z < sin(-1f * Math.toRadians(1.0))) {
                lmgr.setLandscapeTint(Vec3f(1f))
            } else {
                val sunAlt = asin(posAltAz.z)
                val angleFactor = if (sunAlt > 0) 1f else 0.5f * (cos(180f * sunAlt).toFloat() + 1f)

                val extinctedMag = getVMagnitudeWithExtinction(core) - getVMagnitude(core)
                val color = Vec3f(
                    haloColor.x,
                    0.80f.pow(extinctedMag) * haloColor.y,
                    0.25f.pow(extinctedMag) * haloColor.z
                )

                val fullTint =
                    0.25f * Vec3f(3f + sqrt(color.x), 3f + sqrt(color.y), 3f + sqrt(color.z))
                lmgr.setLandscapeTint(angleFactor * fullTint + (1f - angleFactor) * Vec3f(1f))
            }
        }

        val cutDimObjects =
            ((vMagnitude - 5.0f) > core.skyDrawer.limitMagnitude) && pType >= PlanetType.ASTEROID
        if (ss.markerValue == 0.0 && cutDimObjects && !core.currentLocation.planetName.contains(
                "Observer",
                ignoreCase = true
            )
        ) {
            return
        }
        if (pType >= PlanetType.ASTEROID && vMagnitude > ss.markerMagThreshold) return

        var mat = Mat4d.translation(eclipticPos) * rotLocalToParent
        var p = parent

        when (rotationElements.method) {
            RotationElements.ComputationMethod.TRADITIONAL -> {
                while (p?.parent != null) {
                    mat = Mat4d.translation(p.eclipticPos) * mat * p.rotLocalToParent
                    p = p.parent
                }
            }

            RotationElements.ComputationMethod.WGCCRE -> {
                while (p?.parent != null) {
                    mat = Mat4d.translation(p.eclipticPos) * mat
                    p = p.parent
                }
            }
        }
        mat = Mat4d.translation(aberrationPush) * mat

        val transfo = core.getHeliocentricEclipticModelViewTransform()
        transfo.combine(mat)

        if (this == core.currentPlanet) {
            if (rings) draw3dModel(core, transfo, 1024f, eclipseFactor, true)
            return
        }
    }

    suspend fun draw3dModel(
        core: AstralisCore,
        transfo: StelProjector.ModelViewTranformP,
        screenRd: Float,
        solarEclipseFactor: Double,
        drawOnlyRing: Boolean
    ) {
        val ssm = core.getModule(SolarSystem::class.java)

        val vMagnitude = getVMagnitude(core, solarEclipseFactor)
        val vMagnitudeWithExtinction = getVMagnitudeWithExtinction(core, vMagnitude)

        val extinctedMag = vMagnitudeWithExtinction - vMagnitude
        val magFactorGreen = 0.85.pow(0.6 * extinctedMag).toFloat()
        val magFactorBlue = 0.6.pow(0.5 * extinctedMag).toFloat()

        // Draw the real 3D object only if the screen radius is greater than 1.
        if (screenRd > 1f) {
            val n = core.clippingPlanes.first
            val f = core.clippingPlanes.second

            // Adjust clipping planes based on the planet's radius.
            val r = equatorialRadius * sphereScale
            val dist = getEquinoxEquatorialPos(core).norm()
            val zNear = max(0.00001, dist - r)
            val zFar = dist + 10 * r
            core.setClippingPlanes(zNear, zFar)

            val transfo2 = transfo.clone()
            transfo2.combine(Mat4d.zRotation(Math.PI / 180 * (axisRotation + 90f)))
            val sPainter = StelPainter(core.getProjection(transfo2))

            val sunPos = ssm.getSun().heliocentricEclipticPos + ssm.getSun().aberrationPush()
            core.getHeliocentricEclipticModelViewTransform().forward(sunPos)
            val light = Light(sunPos)

            light.diffuse.set(1f, magFactorGreen, magFactorBlue)
            light.ambient.set(0.02f, magFactorGreen * 0.02f, magFactorBlue * 0.02f)

            // Draw 3D model, skipping extra renderings like the halo and the sun's corona
            if (ssm.getFlagUseObjModels() && objModelPath.isNotEmpty()) {
                if (!drawObjModel(sPainter, screenRd)) {
                    drawSphere(sPainter, screenRd, drawOnlyRing)
                }
            } else {
                drawSphere(sPainter, screenRd, drawOnlyRing)
            }

            core.setClippingPlanes(n, f)  // Restore original clipping planes
        }
    }

    // A Planet's own eclipticPos is in VSOP87 ref. frame (practically equal to ecliptic of J2000 for us) coordinates relative to the parent body (sun, planet).
    // To get J2000 equatorial coordinates, we require heliocentric ecliptical positions (adding up parent positions) of observer and Planet.
    // Then we use the matrix rotation multiplication with an existing matrix in StelCore to orient from eclipticalJ2000 to equatorialJ2000.
    // The end result is a non-normalized 3D vector which allows retrieving distances etc.
    // To apply aberration correction, we need the velocity vector of the observer's planet and apply a little correction in SolarSystem::computePositions()
    // prepare for aberration: Explan. Suppl. 2013, (7.38)
    override fun getJ2000EquatorialPos(core: AstralisCore): Vec3d {
        return AstralisCore.matVsop87ToJ2000.multiplyWithoutTranslation(
            getHeliocentricEclipticPos() - core.getObserverHeliocentricEclipticPos() + Vec3d(0.0)
        )
    }

    // Computation of the visual magnitude (V band) of the planet.
    override suspend fun getVMagnitude(core: AstralisCore): Float {
        return getVMagnitude(core, 1.0)
    }

    suspend fun getVMagnitude(core: AstralisCore, eclipseFactor: Double): Float {
        if (parent == null) {
            // Sun, compute the apparent magnitude for the absolute mag (V: 4.83) and observer's distance
            // Hint: Absolute Magnitude of the Sun in Several Bands: http://mips.as.arizona.edu/~cnaw/sun.html
            val distParsec =
                sqrt(core.getObserverHeliocentricEclipticPos().normSquared()) * AU / PARSEC

            // Check how much of it is visible
            val shadowFactor = max(0.000128, eclipseFactor)
            // See: Hughes, D. W., Brightness during a solar eclipse // Journal of the British Astronomical Association, vol.110, no.4, p.203-205
            // URL: http://adsabs.harvard.edu/abs/2000JBAA..110..203H

            return (4.83 + 5 * (log10(distParsec) - 1.0) - 2.5 * (log10(shadowFactor))).toFloat()
        }

        val ssystem: SolarSystem = SolarSystem.getInstance()
        // Compute the phase angle i. We need the intermediate results also below, therefore we don't just call getPhaseAngle.
        val observerHelioPos = core.getObserverHeliocentricEclipticPos()
        val observerRq = observerHelioPos.normSquared()
        val planetHelioPos = getHeliocentricEclipticPos()
        val planetRq = planetHelioPos.normSquared()
        val observerPlanetRq = (observerHelioPos - planetHelioPos).normSquared()
        val dr = sqrt(observerPlanetRq * planetRq)
        val cosChi = (observerPlanetRq + planetRq - observerRq) / (2.0 * dr)
        val phaseAngle = acos(cosChi)

        var shadowFactor = 1.0
        // Check if the satellite is inside the inner shadow of the parent planet:
        parent.parent?.let { parent ->
            val parentHelioPos = parent.getHeliocentricEclipticPos()
            val parentRq = parentHelioPos.normSquared()
            val posTimesParentPos = planetHelioPos * parentHelioPos
            if (posTimesParentPos > parentRq) {
                // The satellite is farther away from the sun than the parent planet.
                if (name == "Moon") {
                    val totalityFactor = 2.710e-5 // defined previously by AW
                    val shadowRadii = ssystem.getEarthShadowRadiiAtLunarDistance()
                    val dist = getEclipticPos().norm()  // Lunar distance [AU]
                    val u =
                        shadowRadii.first[0] / 3600.0 // geocentric angle of earth umbra radius at lunar distance [degrees]
                    val p =
                        shadowRadii.second[0] / 3600.0 // geocentric angle of earth penumbra radius at lunar distance [degrees]
                    val r =
                        atan(equatorialRadius / dist) * (180.0 / Math.PI) // geocentric angle of Lunar radius at lunar distance [degrees]

                    // We must compute an elongation from the aberrated sun. The following is adapted from getElongation(), with a tweak to move the Sun to its apparent position.
                    val sun = ssystem.getSun()
                    val obsPos = parent.eclipticPos - sun.getAberrationPush()
                    val observerRq = obsPos.normSquared()
                    val planetHelioPos = getHeliocentricEclipticPos() - sun.getAberrationPush()
                    val planetRq = planetHelioPos.normSquared()
                    val observerPlanetRq = dist * dist // (obsPos - planetHelioPos).normSquared()
                    val aberratedElongation = Math.acos(
                        (observerPlanetRq + observerRq - planetRq) / (2.0 * Math.sqrt(
                            observerPlanetRq * observerRq
                        ))
                    )
                    val od =
                        180.0 - aberratedElongation * (180.0 / Math.PI) // opposition distance [degrees]

                    when {
                        od > p + r -> shadowFactor = 1.0
                        od > u + r -> shadowFactor = 0.6 + 0.4 * Math.sqrt((od - u - r) / (p - u))
                        od > u - r -> shadowFactor =
                            totalityFactor + (0.6 - totalityFactor) * (od - u + r) / (2.0 * r)

                        else -> shadowFactor = totalityFactor * 0.5 * (1 + od / (u - r))
                    }
                } else {
                    val sunRadius = parent.parent?.equatorialRadius ?: 0.0
                    val sunMinusParentRadius = sunRadius - parent.equatorialRadius
                    val quot = posTimesParentPos / parentRq

                    // Compute d = distance from satellite center to border of inner shadow.
                    // d>0 means inside the shadow cone.
                    var d = sunRadius - sunMinusParentRadius * quot - Math.sqrt(
                        (1.0 - sunMinusParentRadius / Math.sqrt(parentRq)) * (planetRq - posTimesParentPos * quot)
                    )
                    if (d >= equatorialRadius) {
                        // The satellite is totally inside the inner shadow.
                        shadowFactor = 1e-9
                    } else if (d > -equatorialRadius) {
                        // The satellite is partly inside the inner shadow,
                        // compute a fantasy value for the magnitude:
                        d /= equatorialRadius
                        shadowFactor = (0.5 - (Math.asin(d) + d * Math.sqrt(1.0 - d * d)) / Math.PI)
                    }
                }
            }
        }

        // Lunar Magnitude from Earth: This is a combination of Russell 1916 (!) with its albedo dysbalance, Krisciunas-Schaefer (1991) for the opposition surge, and Agrawal (2016) for the contribution of earthshine.
        if (core.getCurrentLocation().planetName == "Earth" && name == "Moon") {
            val observerHelioVelocity = core.getObserverHeliocentricEclipticVelocity()
            val signedPhaseAngle =
                if (isWaning(observerHelioPos, observerHelioVelocity)) phaseAngle else -phaseAngle

            val p = signedPhaseAngle * (180.0 / Math.PI)
            var magIll = if (p < 0) {
                ((((((4.208547E-12 * p + 1.754857E-9) * p + 2.749700E-7) * p + 1.860811E-5) * p + 5.590310E-4) * p - 1.628691E-2) * p + 4.807056E-3
            } else {
                ((((((4.609790E-12 * p - 1.977692E-9) * p + 3.305454E-7) * p - 2.582825E-5) * p + 9.593360E-4) * p + 1.213761E-2) * p + 7.710015E-3
            }
            magIll -= 12.73
            val rf = 2.56e-6  // Reference flux [lx] from Agrawal (14)
            var fluxIll = rf * Math.pow(10.0, -0.4 * magIll)

            // Apply opposition surge where needed
            val surge = Math.max(1.0, 1.35 - 2.865 * Math.abs(phaseAngle))
            fluxIll *= surge // This is now shape of Russell's magnitude curve with peak brightness matched with Krisciunas-Schaefer
            // Apply distance factor
            val lunarMeanDist = 384399.0 / AU
            val lunarMeanDistSq = lunarMeanDist * lunarMeanDist
            fluxIll *= lunarMeanDistSq / observerPlanetRq

            // Compute flux of earthshine: Agrawal 2016.
            val beta = parent.equatorialRadius * parent.equatorialRadius / eclipticPos.normSquared()
            val gamma = equatorialRadius * equatorialRadius / eclipticPos.normSquared()

            val slfoe =
                133100.0 // https://www.allthingslighting.org/index.php/2019/02/15/solar-illumination/
            val LumEarth = slfoe * core.getCurrentObserver().getHomePlanet().albedo
            val elfom = LumEarth * beta
            val elfoe = elfom * albedo * gamma  // Brightness of full earthshine.
            val pfac =
                1.0 - (0.5 * (1.0 + Math.cos(signedPhaseAngle))) // Diminishing earthshine with phase angle
            val fluxTotal = fluxIll + elfoe * pfac
            return (-2.5 * Math.log10(fluxTotal * shadowFactor / rf)).toFloat()
        }

        // Use empirical formulae for main planets when seen from earth. MallamaHilton_2018 also work from other locations.
        if (Planet.getApparentMagnitudeAlgorithm() == MallamaHilton_2018 || core.getCurrentLocation().planetName == "Earth") {
            val phaseDeg = phaseAngle * (180.0 / Math.PI)
            val d = 5.0 * Math.pow((phaseDeg / 180.0), 2.0) - 10.0
            return (this.getMagnitude() + d).toFloat()
        }

        // Simplified Equation for Mars only; value produced by best estimates for single phase
        val planetDistance = this.getHeliocentricEclipticPos().norm()
        val value = Math.sqrt(planetDistance * Math.pow(planetDistance / planetDistance - 1.0, 0.5))

        return (-2.5 * Math.log10(value)).toFloat()
    }


    fun getHeliocentricEclipticPos(): Vec3d = getHeliocentricPos(eclipticPos)


//    suspend fun getHeliocentricEclipticPos(dateJDE: Double): Vec3d {
//        var pos = getEclipticPos(dateJDE)
//
//        var parentPlanet = parent
//        if (parentPlanet != null) {
//            while (parentPlanet?.parent != null) {
//                pos += parentPlanet.getEclipticPos(dateJDE)
//                parentPlanet = parentPlanet.parent
//            }
//        }
//        return pos
//    }

    // Return heliocentric ecliptical Cartesian J2000 coordinates of p [AU]
    fun getHeliocentricPos(p: Vec3d): Vec3d {
        var pos = p
        var pp = parent

        if (pp != null) {
            while (pp?.parent != null) {
                pos += pp.eclipticPos
                pp = pp.parent
            }
        }
        return pos
    }

    // Получение позиции планеты в эклиптических координатах (J2000) в а. е., центрированной на родительской планете
    suspend fun getEclipticPos(dateJDE: Double = lastJDE): Vec3d {
        // Используем текущую позицию, если время совпадает
        if (fuzzyEquals(dateJDE, lastJDE)) {
            return eclipticPos
        }

        // В противном случае пытаемся использовать кэшированную позицию
        val cachedPos = orbitPositionsCache.get(dateJDE)
        if (cachedPos != null) {
            return cachedPos
        }

        // Если позиции нет в кэше, вычисляем её
        val pos = Vec3d()
        val velDummy = Vec3d()
        coordFunc(dateJDE, pos.toDoubleArray(), velDummy.toDoubleArray(), null)

        // Сохраняем в кэш и возвращаем
        orbitPositionsCache.put(dateJDE, pos)
        return pos
    }

    companion object {
        private val asteroidColorMap = mapOf(
            PlanetType.ASTEROID to Vec3f(0.35f, 0.35f, 0.35f),
            PlanetType.PLUTINO to Vec3f(1f, 1f, 0f),
            PlanetType.COMET to Vec3f(0.25f, 0.75f, 1f),
            PlanetType.DWARF_PLANET to Vec3f(1f, 1f, 1f),
            PlanetType.CUBEWANO to Vec3f(1f, 0f, 0.8f),
            PlanetType.SDO to Vec3f(0.5f, 1f, 0.5f),
            PlanetType.OCO to Vec3f(0.75f, 0.75f, 1f),
            PlanetType.SEDNOID to Vec3f(0.75f, 1f, 0.75f),
            PlanetType.INTERSTELLAR to Vec3f(1f, 0.25f, 0.25f),
            PlanetType.UNDEFINED to Vec3f(1f, 0f, 0f)
        )
    }

    // Вычисляет осевое вращение вокруг Z (суточное вращение вокруг полярной оси) [в градусах]
    // для перехода от экваториальных координат к координатам часового угла.
    // Для Земли звездное время — это угол вдоль экватора планеты от RA0 до меридиана,
    // то есть часовой угол первой точки Овна. Для Земли это звездное время в Гринвиче.
    //
    // В версии 0.21+ обновлены данные о вращении для планет и лун.
    // В этом контексте вычисляется угол W главного меридиана от восходящего узла экватора планеты на экваторе ICRF.
    //
    // Нам нужны и JD, и JDE для Земли (для других планет только JDE).
    fun getSiderealTime(JD: Double, JDE: Double): Double {
        if (name == "Earth") {
            return if (StelApp.getInstance().core.useNutation) {
                getApparentSiderealTime(JD, JDE) // В градусах
            } else {
                getMeanSiderealTime(JD, JDE) // В градусах
            }
        }

        // V0.21+: новые значения вращения из ExplSup2013 или WGCCRE2009/2015
        if (rotationElements.method == RotationElements.ComputationMethod.WGCCRE) {
            // Возвращает угол W — долготу главного меридиана, измеренную вдоль экватора планеты
            // от восходящего узла экватора планеты на экваторе ICRF.
            val t = JDE - J2000
            val T = t / 36525.0
            var w =
                rotationElements.W0 + (t * rotationElements.W1) % 360.0 // Ограничиваем угол, чтобы избежать переполнения
            w += rotationElements.corrW(
                t,
                T
            ) // Применяем специфические поправки (ExplSup2013/WGCCRE)
            return w
        }

        // СТАРАЯ МОДЕЛЬ (до V0.21)
        // Используется для объектов, у которых нет современных данных WGCCRE.

        val t = JDE - rotationElements.epoch
        // Избегаем деления на ноль (для лун с хаотичным периодом вращения)
        val rotations = if (rotationElements.period == 0.0) {
            1.0 // Луна с хаотичным периодом вращения
        } else {
            (t / rotationElements.period) % 1.0 // Ограничиваем число полных оборотов
        }

        return rotations * 360.0 + rotationElements.offset
    }
}

// Get observer-centered alt/az position
//fun CelestialObject.getAltAzPosAuto(val core: AstralisCore): Vec3d {
//    return core.j2000ToAltAz(getJ2000EquatorialPos(core), StelCore::RefractionAuto)
//}