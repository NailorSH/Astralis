package com.nailorsh.astralis

import kotlin.math.PI
import kotlin.math.sin

const val J2000 = 2451545.0

class RotationElements(name: String) {
    enum class ComputationMethod {
        TRADITIONAL, // Stellarium prior to 0.21, This is unfortunately not documented.
        WGCCRE // Orientation as described by the IAU Working Group on Cartographic Coordinates and Rotational Elements reports of 2009 or later
    }

    enum class PlanetCorrection {
        EarthMoon, // 3
        Mars, // 4
        Jupiter, // 5
        Saturn, // 6
        Uranus, // 7
        Neptune // 8
    }

    val corrW = axisRotCorrFuncMap.getValue(name)

    var period: Double = 1.0 // [deprecated] (sidereal) rotation period [earth days]
    var offset: Double = 0.0 // [deprecated] rotation at epoch [degrees]
    var epoch: Double = J2000 // JDE (JD TT) of epoch for these elements
    var obliquity: Double = 0.0 // [deprecated] tilt of rotation axis w.r.t. ecliptic [radians]
    var ascendingNode: Double =
        0.0 // [deprecated] long. of ascending node of equator on the ecliptic [radians]
    var method: ComputationMethod =
        ComputationMethod.TRADITIONAL // The reference system in use for the respective object.
    var ra0: Double = 0.0 // [rad] RA_0 right ascension of north pole.
    var ra1: Double = 0.0 // [rad/century] rate of change in axis ra
    var de0: Double = 0.0 // [rad] DE_0 declination of north pole
    var de1: Double = 0.0 // [rad/century] rate of change in axis de
    var W0: Double =
        0.0 // [deg] mean longitude of prime meridian along equator measured from intersection with ICRS plane at epoch.
    var W1: Double = 0.0 // [deg/d] mean longitude motion. W=W0+d*W1.
    var currentAxisRA: Double = 0.0 // [rad] Mostly for infostring: RA=RA0+d*RA1(+corrections)
    var currentAxisDE: Double = 0.0 // [rad] Mostly for infostring: DE=DE0+d*DE1(+corrections)
    var currentAxisW: Double = 0.0 // [deg] Mostly for infostring: W =W0 +d*W1 (+corrections)

    data class PlanetCorrections(
        var JDE_E: Double, // keep record of when these values are valid: Earth
        var JDE_M: Double, // keep record of when these values are valid: Mars
        var JDE_J: Double, // keep record of when these values are valid: Jupiter
        var JDE_S: Double, // keep record of when these values are valid: Saturn
        var JDE_U: Double, // keep record of when these values are valid: Uranus
        var JDE_N: Double, // keep record of when these values are valid: Neptune

        var E1: Double, // Earth corrections
        var E2: Double,
        var E3: Double,
        var E4: Double,
        var E5: Double,
        var E6: Double,
        var E7: Double,
        var E8: Double,
        var E9: Double,
        var E10: Double,
        var E11: Double,
        var E12: Double,
        var E13: Double,

        var M1: Double, // Mars corrections
        var M2: Double,
        var M3: Double,
        var M4: Double,
        var M5: Double,
        var M6: Double,
        var M7: Double,
        var M8: Double,
        var M9: Double,
        var M10: Double,

        var Ja: Double, // Jupiter axis terms
        var Jb: Double,
        var Jc: Double,
        var Jd: Double,
        var Je: Double,

        var Na: Double, // Neptune axis term

        var J1: Double, // corrective terms for Jupiter's moons
        var J2: Double,
        var J3: Double,
        var J4: Double,
        var J5: Double,
        var J6: Double,
        var J7: Double,
        var J8: Double,

        var S1: Double, // corrective terms for Saturn's moons
        var S2: Double,
        var S3: Double,
        var S4: Double,
        var S5: Double,
        var S6: Double,

        var U1: Double, // for Cordelia
        var U2: Double, // for Ophelia
        var U3: Double, // for Bianca
        var U4: Double, // for Cressida
        var U5: Double, // for Desdemona
        var U6: Double, // for Juliet
        var U7: Double, // for Portia
        var U8: Double, // for Rosalind
        var U9: Double, // for Belinda
        var U10: Double, // for Puck
        var U11: Double,
        var U12: Double,
        var U13: Double,
        var U14: Double,
        var U15: Double,
        var U16: Double,

        var N1: Double, // corrective terms for Neptune's moons
        var N2: Double,
        var N3: Double,
        var N4: Double,
        var N5: Double,
        var N6: Double,
        var N7: Double
    )

    companion object {
        var planetCorrections = PlanetCorrections(
            JDE_E = 0.0,
            JDE_M = 0.0,
            JDE_J = 0.0,
            JDE_S = 0.0,
            JDE_U = 0.0,
            JDE_N = 0.0,
            E1 = 0.0,
            E2 = 0.0,
            E3 = 0.0,
            E4 = 0.0,
            E5 = 0.0,
            E6 = 0.0,
            E7 = 0.0,
            E8 = 0.0,
            E9 = 0.0,
            E10 = 0.0,
            E11 = 0.0,
            E12 = 0.0,
            E13 = 0.0,
            M1 = 0.0,
            M2 = 0.0,
            M3 = 0.0,
            M4 = 0.0,
            M5 = 0.0,
            M6 = 0.0,
            M7 = 0.0,
            M8 = 0.0,
            M9 = 0.0,
            M10 = 0.0,
            Ja = 0.0,
            Jb = 0.0,
            Jc = 0.0,
            Jd = 0.0,
            Je = 0.0,
            Na = 0.0,
            J1 = 0.0,
            J2 = 0.0,
            J3 = 0.0,
            J4 = 0.0,
            J5 = 0.0,
            J6 = 0.0,
            J7 = 0.0,
            J8 = 0.0,
            S1 = 0.0,
            S2 = 0.0,
            S3 = 0.0,
            S4 = 0.0,
            S5 = 0.0,
            S6 = 0.0,
            U1 = 0.0,
            U2 = 0.0,
            U3 = 0.0,
            U4 = 0.0,
            U5 = 0.0,
            U6 = 0.0,
            U7 = 0.0,
            U8 = 0.0,
            U9 = 0.0,
            U10 = 0.0,
            U11 = 0.0,
            U12 = 0.0,
            U13 = 0.0,
            U14 = 0.0,
            U15 = 0.0,
            U16 = 0.0,
            N1 = 0.0,
            N2 = 0.0,
            N3 = 0.0,
            N4 = 0.0,
            N5 = 0.0,
            N6 = 0.0,
            N7 = 0.0
        )

        // Also include the GRS corrections here
        val grsJD = 2456901.50
        val grsLongitude = 216.0
        val grsDrift = 15.0

        val axisRotCorrFuncMap: Map<String, (Double, Double) -> Double> = mapOf(
            "Moon" to ::corrWMoon,
            "Mercury" to ::corrWMercury,
            "Mars" to ::corrWMars,
            "Jupiter" to ::corrWJupiter,
            "Neptune" to ::corrWNeptune,
            "Phobos" to ::corrWPhobos,
            "Deimos" to ::corrWDeimos,
            "Io" to ::corrWIo,
            "Europa" to ::corrWEuropa,
            "Ganymede" to ::corrWGanymede,
            "Callisto" to ::corrWCallisto,
            "Amalthea" to ::corrWAmalthea,
            "Thebe" to ::corrWThebe,
            "Mimas" to ::corrWMimas,
            "Tethys" to ::corrWTethys,
            "Rhea" to ::corrWRhea,
            "Janus" to ::corrWJanus,
            "Epimetheus" to ::corrWEpimetheus,
            "Cordelia" to ::corrWCordelia,
            "Ophelia" to ::corrWOphelia,
            "Bianca" to ::corrWBianca,
            "Cressida" to ::corrWCressida,
            "Desdemona" to ::corrWDesdemona,
            "Juliet" to ::corrWJuliet,
            "Portia" to ::corrWPortia,
            "Rosalind" to ::corrWRosalind,
            "Belinda" to ::corrWBelinda,
            "Puck" to ::corrWPuck,
            "Ariel" to ::corrWAriel,
            "Umbriel" to ::corrWUmbriel,
            "Titania" to ::corrWTitania,
            "Oberon" to ::corrWOberon,
            "Miranda" to ::corrWMiranda,
            "Triton" to ::corrWTriton,
            "Naiad" to ::corrWNaiad,
            "Thalassa" to ::corrWThalassa,
            "Despina" to ::corrWDespina,
            "Galatea" to ::corrWGalatea,
            "Larissa" to ::corrWLarissa,
            "Proteus" to ::corrWProteus
        )

        fun corrWMoon(d: Double, T: Double): Double {
            return -1.4e-12 * d * d + 3.5610 * sin(planetCorrections.E1) +
                    0.1208 * sin(planetCorrections.E2) - 0.0642 * sin(planetCorrections.E3) + 0.0158 * sin(
                planetCorrections.E4
            ) +
                    0.0252 * sin(planetCorrections.E5) - 0.0066 * sin(planetCorrections.E6) - 0.0047 * sin(
                planetCorrections.E7
            ) -
                    0.0046 * sin(planetCorrections.E8) + 0.0028 * sin(planetCorrections.E9) + 0.0052 * sin(
                planetCorrections.E10
            ) +
                    0.0040 * sin(planetCorrections.E11) + 0.0019 * sin(planetCorrections.E12) - 0.0044 * sin(
                planetCorrections.E13
            )
        }

        fun corrWMercury(d: Double, T: Double): Double {
            val M1 = (174.7910857 * PI / 180) + (4.092335 * PI / 180 * d).mod(2.0 * PI)
            val M2 = (349.5821714 * PI / 180) + (8.184670 * PI / 180 * d).mod(2.0 * PI)
            val M3 = (164.3732571 * PI / 180) + (12.277005 * PI / 180 * d).mod(2.0 * PI)
            val M4 = (339.1643429 * PI / 180) + (16.369340 * PI / 180 * d).mod(2.0 * PI)
            val M5 = (153.9554286 * PI / 180) + (20.461675 * PI / 180 * d).mod(2.0 * PI)

            return -0.00000571 * sin(M5) - 0.00002539 * sin(M4) - 0.00011040 * sin(M3) - 0.00112309 * sin(
                M2
            ) + 0.01067257 * sin(M1)
        }

        fun corrWMars(d: Double, T: Double): Double {
            return 0.000145 * sin(PI / 180 * (129.071773 + (19140.0328244 * T).mod(360.0))) +
                    0.000157 * sin(PI / 180 * (36.352167 + (38281.0473591 * T).mod(360.0))) +
                    0.000040 * sin(PI / 180 * (56.668646 + (57420.9295360 * T).mod(360.0))) +
                    0.000001 * sin(PI / 180 * (67.364003 + (76560.2552215 * T).mod(360.0))) +
                    0.000001 * sin(PI / 180 * (104.792680 + (95700.4387578 * T).mod(360.0))) +
                    0.584542 * sin(PI / 180 * (95.391654 + (0.5042615 * T).mod(360.0)))
        }

        fun corrWJupiter(d: Double, T: Double): Double {
            val JDE = d + J2000
            val longitudeGRS = grsLongitude + grsDrift * (JDE - grsJD) / 365.25
            return -longitudeGRS + ((187.0 / 512.0) * 360.0 - 90.0)
        }

        fun corrWNeptune(d: Double, T: Double): Double {
            return -0.48 * sin(planetCorrections.Na)
        }

        fun corrWPhobos(d: Double, T: Double): Double {
            return 12.72192797 * T * T +
                    1.42421769 * sin(planetCorrections.M1) -
                    0.02273783 * sin(planetCorrections.M2) +
                    0.00410711 * sin(planetCorrections.M3) +
                    0.00631964 * sin(planetCorrections.M4) -
                    1.143 * sin(planetCorrections.M5)
        }

        fun corrWDeimos(d: Double, T: Double): Double {
            return -2.73954829 * sin(planetCorrections.M6) -
                    0.39968606 * sin(planetCorrections.M7) -
                    0.06563259 * sin(planetCorrections.M8) -
                    0.02912940 * sin(planetCorrections.M9) +
                    0.01699160 * sin(planetCorrections.M10)
        }

        fun corrWIo(d: Double, T: Double): Double {
            return -0.085 * sin(planetCorrections.J3) - 0.022 * sin(planetCorrections.J4)
        }

        fun corrWEuropa(d: Double, T: Double): Double {
            return -0.980 * sin(planetCorrections.J4) - 0.054 * sin(planetCorrections.J5) -
                    0.014 * sin(planetCorrections.J6) - 0.008 * sin(planetCorrections.J7)
        }

        fun corrWGanymede(d: Double, T: Double): Double {
            return 0.033 * sin(planetCorrections.J4) - 0.389 * sin(planetCorrections.J5) -
                    0.082 * sin(planetCorrections.J6)
        }

        fun corrWCallisto(d: Double, T: Double): Double {
            return 0.061 * sin(planetCorrections.J5) - 0.533 * sin(planetCorrections.J6) -
                    0.009 * sin(planetCorrections.J8)
        }

        fun corrWAmalthea(d: Double, T: Double): Double {
            return 0.76 * sin(planetCorrections.J1) - 0.01 * sin(2.0 * planetCorrections.J1)
        }

        fun corrWThebe(d: Double, T: Double): Double {
            return 1.91 * sin(planetCorrections.J2) - 0.04 * sin(2.0 * planetCorrections.J2)
        }

        fun corrWMimas(d: Double, T: Double): Double {
            return -13.48 * sin(planetCorrections.S3) - 44.85 * sin(planetCorrections.S5)
        }

        fun corrWTethys(d: Double, T: Double): Double {
            return -9.60 * sin(planetCorrections.S4) + 2.23 * sin(planetCorrections.S5)
        }

        fun corrWRhea(d: Double, T: Double): Double {
            return -3.08 * sin(planetCorrections.S6)
        }

        fun corrWJanus(d: Double, T: Double): Double {
            return 1.613 * sin(planetCorrections.S2) - 0.023 * sin(2.0 * planetCorrections.S2)
        }

        fun corrWEpimetheus(d: Double, T: Double): Double {
            return 3.133 * sin(planetCorrections.S1) - 0.086 * sin(2.0 * planetCorrections.S1)
        }

        fun corrWCordelia(d: Double, T: Double): Double {
            return -0.04 * sin(planetCorrections.U1)
        }

        fun corrWOphelia(d: Double, T: Double): Double {
            return -0.03 * sin(planetCorrections.U2)
        }

        fun corrWBianca(d: Double, T: Double): Double {
            return -0.04 * sin(planetCorrections.U3)
        }

        fun corrWCressida(d: Double, T: Double): Double {
            return -0.01 * sin(planetCorrections.U4)
        }

        fun corrWDesdemona(d: Double, T: Double): Double {
            return -0.04 * sin(planetCorrections.U5)
        }

        fun corrWJuliet(d: Double, T: Double): Double {
            return -0.02 * sin(planetCorrections.U6)
        }

        fun corrWPortia(d: Double, T: Double): Double {
            return -0.02 * sin(planetCorrections.U7)
        }

        fun corrWRosalind(d: Double, T: Double): Double {
            return -0.08 * sin(planetCorrections.U8)
        }

        fun corrWBelinda(d: Double, T: Double): Double {
            return -0.01 * sin(planetCorrections.U9)
        }

        fun corrWPuck(d: Double, T: Double): Double {
            return -0.09 * sin(planetCorrections.U10)
        }

        fun corrWAriel(d: Double, T: Double): Double {
            return 0.05 * sin(planetCorrections.U12) + 0.08 * sin(planetCorrections.U13)
        }

        fun corrWUmbriel(d: Double, T: Double): Double {
            return -0.09 * sin(planetCorrections.U12) + 0.06 * sin(planetCorrections.U14)
        }

        fun corrWTitania(d: Double, T: Double): Double {
            return 0.08 * sin(planetCorrections.U15)
        }

        fun corrWOberon(d: Double, T: Double): Double {
            return 0.04 * sin(planetCorrections.U16)
        }

        fun corrWMiranda(d: Double, T: Double): Double {
            return -1.27 * sin(planetCorrections.U12) + 0.15 * sin(2.0 * planetCorrections.U12) +
                    1.15 * sin(planetCorrections.U11) - 0.09 * sin(2.0 * planetCorrections.U11)
        }

        fun corrWTriton(d: Double, T: Double): Double {
            return 22.25 * sin(planetCorrections.N7) + 6.73 * sin(2.0 * planetCorrections.N7) +
                    2.05 * sin(3.0 * planetCorrections.N7) + 0.74 * sin(4.0 * planetCorrections.N7) +
                    0.28 * sin(5.0 * planetCorrections.N7) + 0.11 * sin(6.0 * planetCorrections.N7) +
                    0.05 * sin(7.0 * planetCorrections.N7) + 0.02 * sin(8.0 * planetCorrections.N7) +
                    0.01 * sin(9.0 * planetCorrections.N7)
        }

        fun corrWNaiad(d: Double, T: Double): Double {
            return -0.48 * sin(planetCorrections.Na) + 4.40 * sin(planetCorrections.N1) - 0.27 * sin(
                2.0 * planetCorrections.N1
            )
        }

        fun corrWThalassa(d: Double, T: Double): Double {
            return -0.48 * sin(planetCorrections.Na) + 0.19 * sin(planetCorrections.N2)
        }

        fun corrWDespina(d: Double, T: Double): Double {
            return -0.49 * sin(planetCorrections.Na) + 0.06 * sin(planetCorrections.N3)
        }

        fun corrWGalatea(d: Double, T: Double): Double {
            return -0.48 * sin(planetCorrections.Na) + 0.05 * sin(planetCorrections.N4)
        }

        fun corrWLarissa(d: Double, T: Double): Double {
            return -0.48 * sin(planetCorrections.Na) + 0.19 * sin(planetCorrections.N5)
        }

        fun corrWProteus(d: Double, T: Double): Double {
            return -0.48 * sin(planetCorrections.Na) + 0.04 * sin(planetCorrections.N6)
        }
    }
}

