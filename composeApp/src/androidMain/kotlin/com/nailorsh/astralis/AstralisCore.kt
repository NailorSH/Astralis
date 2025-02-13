package com.nailorsh.astralis

import com.nailorsh.astralis.core.utils.graphic.jd_fits_de430
import com.nailorsh.astralis.core.utils.graphic.jd_fits_de431
import com.nailorsh.astralis.core.utils.graphic.jd_fits_de440
import com.nailorsh.astralis.core.utils.graphic.jd_fits_de441
import com.nailorsh.astralis.core.utils.graphic.math.PI_180
import com.nailorsh.astralis.core.utils.graphic.math.matrix.Mat4d
import com.nailorsh.astralis.core.utils.graphic.math.vector.Vec3d
import com.nailorsh.astralis.core.utils.time.currentInstant
import com.nailorsh.astralis.core.utils.time.getDateFromJulianDay
import com.nailorsh.astralis.core.utils.time.getDeltaTByEspenakMeeus
import com.nailorsh.astralis.core.utils.time.getMoonSecularAcceleration
import com.nailorsh.astralis.core.utils.time.yearFraction
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.sin


data class MutablePair<A, B>(var first: A, var second: B)

class AstralisCore {
    private var skyDrawer: SkyDrawer? = null

    // Matrices used for every coordinate transfo
    // Transform from heliocentric ecliptic Cartesian (VSOP87A) to topocentric (StelObserver) altazimuthal coordinate
    private var matHeliocentricEclipticJ2000ToAltAz = Mat4d()

    // Transform from topocentric (StelObserver) altazimuthal coordinate to heliocentric
    // ecliptic Cartesian (VSOP87A)
    private var matAltAzToHeliocentricEclipticJ2000 = Mat4d()

    // Transform from topocentric altazimuthal coordinate to Earth Equatorial
    private var matAltAzToEquinoxEqu = Mat4d()

    // Transform from Earth Equatorial to topocentric (StelObserver) altazimuthal coordinate
    private var matEquinoxEquToAltAz = Mat4d()

    // Transform from topocentric altazimuthal coordinate to Fixed Equatorial system (hour angle/decl)
    var matAltAzToFixedEquatorial = Mat4d()

    // Transform from Fixed Equatorial (hour angle/decl) to topocentric (StelObserver) altazimuthal coordinate
    var matFixedEquatorialToAltAz = Mat4d()

    // Transform from heliocentric ecliptic Cartesian (VSOP87A) to earth equatorial coordinate
    private var matHeliocentricEclipticToEquinoxEqu = Mat4d()

    // For Earth, this is almost the inverse precession matrix, =Rz(VSOPbias)Rx(eps0)Rz(-psiA)Rx(-omA)Rz(chiA)
    private var matEquinoxEquDateToJ2000 = Mat4d()
    private var matJ2000ToEquinoxEqu = Mat4d()

    private var matJ2000ToAltAz = Mat4d()
    private var matAltAzToJ2000 = Mat4d()

    private var position: AstralisObserver? = null
    private var JD = MutablePair(0.0, 0.0)
    private var currentDeltaTAlgorithm = DeltaTAlgorithm.ESPENAK_MEEUS

    private var jdOfLastJDUpdate = 0.0         // JD when the time rate or time last changed
    private var milliSecondsOfLastJDUpdate = 0L

    // Variables for equations of DeltaT
    private var deltaTCustomEquationCoeff = Vec3d()
    private var deltaTnDot = -26.0
    private var deltaTCustomNDot = -26.0
    private var deltaTCustomYear = 1820.0

    // true if the currently selected algorithm does not do a lunar correction
    private var deltaTdontUseMoon = false

    private val deltaTfunc: (JD: Double) -> Double = ::getDeltaTByEspenakMeeus

    // Variables for DE430/431/440/441 ephem calculation
    private var de430Available: Boolean = false // ephem file found
    private var de431Available: Boolean = false // ephem file found
    private var de430Active: Boolean = false    // available and user-activated.
    private var de431Active: Boolean = false    // available and user-activated.
    private var de440Available: Boolean = false // ephem file found
    private var de441Available: Boolean = false // ephem file found
    private var de440Active: Boolean = false    // available and user-activated.
    private var de441Active: Boolean = false    // available and user-activated.
    private val minMaxEphemRange = Pair(0, 0)


    enum class RefractionMode {
        AUTO,  // Automatically decide to add refraction if atmosphere is activated
        ON,  // Always add refraction (i.e. apparent coordinates)
        OFF // Never add refraction (i.e. geometric coordinates)
    }

    enum class DeltaTAlgorithm {
        WITHOUT_CORRECTION,               // Without correction, DeltaT is Zero. Like Stellarium versions before 0.12.
        SCHOCH,                          // Schoch (1931) algorithm for DeltaT
        CLEMENCE,                        // Clemence (1948) algorithm for DeltaT
        IAU,                             // IAU (1952) algorithm for DeltaT (based on observations by Spencer Jones (1939))
        ASTRONOMICAL_EPHEMERIS,           // Astronomical Ephemeris (1960) algorithm for DeltaT
        TUCKERMAN_GOLDSTINE,              // Tuckerman (1962, 1964) & Goldstine (1973) algorithm for DeltaT
        MULLER_STEPHENSON,                // Muller & Stephenson (1975) algorithm for DeltaT
        STEPHENSON_1978,                  // Stephenson (1978) algorithm for DeltaT
        SCHMADEL_ZECH_1979,                // Schmadel & Zech (1979) algorithm for DeltaT
        MORRISON_STEPHENSON_1982,          // Morrison & Stephenson (1982) algorithm for DeltaT (used by RedShift)
        STEPHENSON_MORRISON_1984,          // Stephenson & Morrison (1984) algorithm for DeltaT
        STEPHENSON_HOULDEN,               // Stephenson & Houlden (1986) algorithm for DeltaT
        ESPENAK,                         // Espenak (1987, 1989) algorithm for DeltaT
        BORKOWSKI,                       // Borkowski (1988) algorithm for DeltaT
        SCHMADEL_ZECH_1988,                // Schmadel & Zech (1988) algorithm for DeltaT
        CHAPRONT_TOUZE,                   // Chapront-Touzé & Chapront (1991) algorithm for DeltaT
        STEPHENSON_MORRISON_1995,          // Stephenson & Morrison (1995) algorithm for DeltaT
        STEPHENSON_1997,                  // Stephenson (1997) algorithm for DeltaT
        CHAPRONT_MEEUS,                   // Chapront, Chapront-Touze & Francou (1997) & Meeus (1998) algorithm for DeltaT
        JPL_HORIZONS,                     // JPL Horizons algorithm for DeltaT
        MEEUS_SIMONS,                     // Meeus & Simons (2000) algorithm for DeltaT
        MONTENBRUCK_PFLEGER,              // Montenbruck & Pfleger (2000) algorithm for DeltaT
        REINGOLD_DERSHOWITZ,              // Reingold & Dershowitz (2002, 2007) algorithm for DeltaT
        MORRISON_STEPHENSON_2004,          // Morrison & Stephenson (2004, 2005) algorithm for DeltaT
        REIJS,                           // Reijs (2006) algorithm for DeltaT
        ESPENAK_MEEUS,                    // Espenak & Meeus (2006) algorithm for DeltaT
        ESPENAK_MEEUS_MODIFIED,            // Espenak & Meeus (2006) algorithm with modified formulae for DeltaT (Recommended, default)
        ESPENAK_MEEUS_ZERO_MOON_ACCEL,       // Espenak & Meeus (2006) algorithm for DeltaT (but without additional Lunar acceleration. FOR TESTING ONLY, NONPUBLIC)
        BANJEVIC,                        // Banjevic (2006) algorithm for DeltaT
        ISLAM_SADIQ_QURESHI,               // Islam, Sadiq & Qureshi (2008 + revisited 2013) algorithm for DeltaT (6 polynomials)
        KHALID_SULTANA_ZAIDI,              // M. Khalid, Mariam Sultana and Faheem Zaidi polynomial approximation of time period 1620-2013 (2014)
        STEPHENSON_MORRISON_HOHENKERK_2016, // Stephenson, Morrison, Hohenkerk (2016) RSPA paper provides spline fit to observations for -720..2019 and else parabolic fit.
        HENRIKSSON_2017,                  // Henriksson (2017) algorithm for DeltaT (The solution for Schoch formula for DeltaT (1931), but with ndot=-30.128"/cy^2)
        CUSTOM                           // User defined coefficients for quadratic equation for DeltaT
    }

    fun init() {

    }

    fun updateTransformMatrices() {
        matAltAzToEquinoxEqu = position.getRotAltAzToEquatorial(getJD(), getJDE())
        matEquinoxEquToAltAz = matAltAzToEquinoxEqu.transpose()

        // multiply static J2000 earth axis tilt (eclipticalJ2000<->equatorialJ2000)
        // in effect, this matrix transforms from VSOP87 ecliptical J2000 to planet-based equatorial coordinates.
        // For earth, matJ2000ToEquinoxEqu is the precession matrix.
        matEquinoxEquDateToJ2000 = matVsop87ToJ2000 * position.getRotEquatorialToVsop87()
        matJ2000ToEquinoxEqu = matEquinoxEquDateToJ2000.transpose()
        matJ2000ToAltAz = matEquinoxEquToAltAz * matJ2000ToEquinoxEqu
        matAltAzToJ2000 = matJ2000ToAltAz.transpose()

        matHeliocentricEclipticToEquinoxEqu =
            matJ2000ToEquinoxEqu * matVsop87ToJ2000 * Mat4d.translation(-position.getCenterVsop87Pos())

        // These two next have to take into account the position of the observer on the earth/planet of observation.
        val matAltAzToVsop87 = matJ2000ToVsop87 * matEquinoxEquDateToJ2000 * matAltAzToEquinoxEqu

        val offset =
            position.getTopographicOffsetFromCenter() // [rho cosPhi', rho sinPhi', phi'_rad, rho]
        val sigma = position.getCurrentLocation().getLatitude().toRadians() - offset.v[2]
        val rho = offset.v[3]

        matAltAzToHeliocentricEclipticJ2000 =
            Mat4d.translation(position.getCenterVsop87Pos()) * matAltAzToVsop87 *
                    Mat4d.translation(Vec3d(rho * sin(sigma), 0.0, rho * cos(sigma)))

        matHeliocentricEclipticJ2000ToAltAz =
            Mat4d.translation(Vec3d(-rho * sin(sigma), 0.0, -rho * cos(sigma))) *
                    matAltAzToVsop87.transpose() * Mat4d.translation(-position.getCenterVsop87Pos())
    }

    fun j2000ToEquinoxEqu(v: Vec3d, refMode: RefractionMode): Vec3d {
        if (refMode == RefractionMode.OFF ||
            skyDrawer == null ||
            (refMode == RefractionMode.AUTO && skyDrawer?.flagHasAtmosphere == false)
        ) return matJ2000ToEquinoxEqu * v

        v.transfo4d(matJ2000ToAltAz)
        skyDrawer?.refraction?.forward(v)
        v.transfo4d(matAltAzToEquinoxEqu)
        return v
    }

    // Return the observer heliocentric position
    fun getObserverHeliocentricEclipticPos(): Vec3d {
        return Vec3d(
            matAltAzToHeliocentricEclipticJ2000[12],
            matAltAzToHeliocentricEclipticJ2000[13],
            matAltAzToHeliocentricEclipticJ2000[14]
        )
    }


    companion object {
        //! Rotation matrix from equatorial J2000 to ecliptic (VSOP87A).
        val matJ2000ToVsop87: Mat4d = Mat4d.xRotation(-23.439280305555556 * PI_180) *
                Mat4d.zRotation(0.0000275 * PI_180)

        //! Rotation matrix from ecliptic (VSOP87A) to equatorial J2000.
        val matVsop87ToJ2000 = matJ2000ToVsop87.transpose()

        //! Rotation matrix from J2000 to Galactic reference frame, using FITS convention.
        val matJ2000ToGalactic = Mat4d(
            -0.054875539726, 0.494109453312, -0.867666135858, 0,
            -0.873437108010, -0.444829589425, -0.198076386122, 0,
            -0.483834985808, 0.746982251810, 0.455983795705, 0,
            0, 0, 0, 1
        )

        //! Rotation matrix from Galactic to J2000 reference frame, using FITS convention.
        val matGalacticToJ2000 = matJ2000ToGalactic.transpose()

        //! Rotation matrix from J2000 to Supergalactic reference frame.
        val matJ2000ToSupergalactic = Mat4d(
            0.37501548, -0.89832046, 0.22887497, 0,
            0.34135896, -0.09572714, -0.93504565, 0,
            0.86188018, 0.42878511, 0.27075058, 0,
            0, 0, 0, 1
        )

        //! Rotation matrix from Supergalactic to J2000 reference frame.
        val matSupergalacticToJ2000 = matJ2000ToSupergalactic.transpose()
    }

    fun setJD(newJD: Double) {
        JD = JD.copy(newJD, computeDeltaT(newJD))
        resetSync()
    }

    fun getJD(): Double {
        return JD.first
    }

    fun setJDE(newJDE: Double) {
        // nitpickerish this is not exact, but as good as it gets...
        JD.second = computeDeltaT(newJDE)
        JD.first = newJDE - JD.second / 86400.0
        resetSync()
    }

    fun getJDE(): Double {
        return JD.first + JD.second / 86400.0
    }


    fun computeDeltaT(JD: Double): Double {
        var deltaT: Double
        if (currentDeltaTAlgorithm == DeltaTAlgorithm.CUSTOM) {
            // Пользовательские коэффициенты для квадратичного уравнения для DeltaT могут часто изменяться.
            deltaTnDot = deltaTCustomNDot  // n.dot = пользовательское значение "/cy/cy"

            // Получаем год, месяц и день из Юлианского дня.
            val (year, month, day) = getDateFromJulianDay(JD)

            // Вычисляем u.
            val u = (yearFraction(year, month, day) - deltaTCustomYear) / 100.0

            // Применяем уравнение DeltaT.
            deltaT = deltaTCustomEquationCoeff.x +
                    u * (deltaTCustomEquationCoeff.y + u * deltaTCustomEquationCoeff.z)
        } else {
            // Проверка, что deltaTfunc не null и вызов функции для вычисления DeltaT.
            requireNotNull(deltaTfunc) { "DeltaT function must not be null" }
            deltaT = deltaTfunc(JD)
        }

        // Если не нужно учитывать Луну, прибавляем ускорение Луны.
        if (!deltaTdontUseMoon) {
            deltaT += getMoonSecularAcceleration(
                JD,
                deltaTnDot,
                (de440Active && jd_fits_de440(JD)) ||
                        (de441Active && jd_fits_de441(JD)) ||
                        (de430Active && jd_fits_de430(JD)) ||
                        (de431Active && jd_fits_de431(JD))
            )
        }

        return deltaT
    }

    fun resetSync() {
        jdOfLastJDUpdate = getJD()
        milliSecondsOfLastJDUpdate = currentInstant().toEpochMilliseconds()
    }
}