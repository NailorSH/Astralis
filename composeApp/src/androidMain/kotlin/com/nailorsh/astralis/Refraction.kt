//package com.nailorsh.astralis
//
//import com.nailorsh.astralis.SkyDrawer.Companion.MIN_GEO_ALTITUDE_DEG
//import com.nailorsh.astralis.SkyDrawer.Companion.TRANSITION_WIDTH_GEO_DEG
//import com.nailorsh.astralis.core.utils.graphic.math.matrix.Mat4d
//import com.nailorsh.astralis.core.utils.graphic.math.vector.Vec3d
//import kotlin.math.PI
//import kotlin.math.abs
//
//class Refraction {
//    private var pressTempCorr: Float = 0f
//
//    // Used to pretransform coordinates into AltAz frame.
//    private var preTransfoMat = Mat4d()
//
//    // Used to postransform refracted coordinates from AltAz to view.
//    private var postTransfoMat = Mat4d()
//
//    fun innerRefractionForward(altAzPos: Vec3d) {
//        val length = altAzPos.norm()
//        if (length == 0.0) return
//
//        // NOTE: вычисления должны быть в Double, иначе появятся колебания у небольших объектов вроде Каллисто или Тебы
//        require(length > 0.0)
//        val sinGeo = altAzPos.z / length
//        require(abs(sinGeo) <= 1.0)
//
//        var geomAltRad = kotlin.math.asin(sinGeo)
//        var geomAltDeg = 180.0 / PI * geomAltRad
//
//        if (geomAltDeg > MIN_GEO_ALTITUDE_DEG) {
//            // Формула рефракции из Saemundsson, S&T 1986 p70 / Meeus, Astr. Alg.
//            val r =
//                pressTempCorr * (1.02 / kotlin.math.tan((geomAltDeg + 10.3 / (geomAltDeg + 5.11)) * Math.PI / 180.0) + 0.0019279)
//            geomAltDeg += r
//            if (geomAltDeg > 90.0) geomAltDeg = 90.0
//        } else if (geomAltDeg > MIN_GEO_ALTITUDE_DEG - TRANSITION_WIDTH_GEO_DEG) {
//            // Избегаем скачка ниже -5°, интерполируя линейно между MIN_GEO_ALTITUDE_DEG и нижней границей переходной зоны
//            val rM5 =
//                pressTempCorr * (1.02 / kotlin.math.tan((MIN_GEO_ALTITUDE_DEG + 10.3 / (MIN_GEO_ALTITUDE_DEG + 5.11)) * Math.PI / 180.0) + 0.0019279)
//            geomAltDeg += rM5 * (geomAltDeg - (MIN_GEO_ALTITUDE_DEG - TRANSITION_WIDTH_GEO_DEG)) / TRANSITION_WIDTH_GEO_DEG
//        } else {
//            return
//        }
//
//        // На этом этапе скорректирована геометрическая высота.
//        // Если просто изменить altAzPos[2], изменится длина вектора, что повлияет на углы.
//        // Нужно также уменьшить X, Y компоненты на разницу в косинусах высоты.
//        val refrAltRad = geomAltDeg * Math.PI / 180.0
//        val sinRef = kotlin.math.sin(refrAltRad)
//
//        val shortenXY =
//            if (kotlin.math.abs(sinGeo) >= 1.0) 1.0 else kotlin.math.sqrt((1.0 - sinRef * sinRef) / (1.0 - sinGeo * sinGeo))
//
//        altAzPos.x *= shortenXY
//        altAzPos.y *= shortenXY
//        altAzPos.z = sinRef * length
//    }
//
//    fun forward(altAzPos: Vec3d) {
//        altAzPos.transfo4d(preTransfoMat)
//        innerRefractionForward(altAzPos)
//        altAzPos.transfo4d(postTransfoMat)
//    }
//}