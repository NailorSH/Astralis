package com.nailorsh.astralis.core.utils.graphic

fun init_de430(filepath: String) {
    TODO()
}

fun init_de431(filepath: String) {
    TODO()
}

fun jd_fits_de430(jd: Double): Boolean = jd > 2287184.5 && jd < 2688976.5

//Correct limits found via jpl_get_double(). Limits hardcoded to avoid calls each time.
//return !(jd < -3027215.5 || jd > 7930192.5);
//This limits inside those where sun can jump between ecliptic of date and ecliptic2000.
// We lose a month in -13000 and a few months in +17000, this should not matter.
fun jd_fits_de431(jd: Double): Boolean = jd > -3027188.25 && jd < 7930056.87916

fun use_de430(jd: Double): Boolean {}
fun use_de431(jd: Double): Boolean {}
fun init_de440(filepath: String) {
    TODO()
}

fun init_de441(filepath: String) {
    TODO()
}

fun jd_fits_de440(jd: Double): Boolean = jd > 2287184.5 && jd < 2688976.5

//Correct limits found via jpl_get_double(). Limits hardcoded to avoid calls each time.
//return !(jd < -3027215.5 || jd > 7930192.5);
//This limits inside those where sun can jump between ecliptic of date and ecliptic2000.
// We lose a month in -13000 and a few months in +17000, this should not matter.
fun jd_fits_de441(jd: Double): Boolean = jd > -3027188.25 && jd < 7930056.87916
fun use_de440(jd: Double): Boolean {}
fun use_de441(jd: Double): Boolean {}