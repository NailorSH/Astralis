package com.nailorsh.astralis

class SkyDrawer(core: AstralisCore) {
    var flagHasAtmosphere = true
    val refraction = Refraction()

    companion object {
        const val MIN_GEO_ALTITUDE_DEG = -3.54f
        const val MIN_APP_ALTITUDE_DEG = -3.21783f
        const val TRANSITION_WIDTH_GEO_DEG = 1.46f
        const val TRANSITION_WIDTH_APP_DEG = 1.78217f
    }
}