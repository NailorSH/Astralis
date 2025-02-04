package com.nailorsh.astralis.core.data.local.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlanetData(
    val name: String,
    @SerialName("absolute_magnitude") val absoluteMagnitude: Double? = null,
    val albedo: Double? = null,
    val atmosphere: Int = 0,
    val color: String? = null,
    @SerialName("color_index_bv") val colorIndexBV: Double? = null,
    @SerialName("coord_func") val coordFunc: String? = null,
    @SerialName("orbit_period") val orbitPeriod: Double? = null,
    val radius: Double? = null,
    val oblateness: Double? = null,
    @SerialName("rot_equator_ascending_node") val rotationEquatorAscendingNode: Double? = null,
    @SerialName("rot_obliquity") val rotationObliquity: Double? = null,
    @SerialName("rot_periode") val rotationPeriod: Double? = null,
    @SerialName("rot_rotation_offset") val rotationOffset: Double? = null,
    @SerialName("rot_pole_ra") val rotationPoleRA: Double? = null,
    @SerialName("rot_pole_ra1") val rotationPoleRA1: Double? = null,
    @SerialName("rot_pole_de") val rotationPoleDE: Double? = null,
    @SerialName("rot_pole_de1") val rotationPoleDE1: Double? = null,
    @SerialName("rot_pole_w0") val rotationPoleW0: Double? = null,
    @SerialName("rot_pole_w1") val rotationPoleW1: Double? = null,
    @SerialName("tex_map") val textureMap: String? = null,
    @SerialName("mass_kg") val massKg: Double? = null,
    val type: String
) {
    fun getColorRGB(): Color? {
        return color?.split(",")
            ?.map { it.trim().toFloat() }
            ?.takeIf { it.size == 3 }
            ?.let { Color(it[0], it[1], it[2], 1f) }
    }
}
