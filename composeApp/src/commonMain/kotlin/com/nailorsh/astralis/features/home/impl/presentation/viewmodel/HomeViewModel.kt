package com.nailorsh.astralis.features.home.impl.presentation.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import co.touchlab.kermit.Logger
import com.nailorsh.astralis.core.arch.BaseViewModel
import com.nailorsh.astralis.core.coroutines.AppDispatchers
import com.nailorsh.astralis.core.data.domain.SolarSystemPlanetsRadius
import com.nailorsh.astralis.core.data.remote.AstronomyApi
import com.nailorsh.astralis.core.data.remote.model.BodyPositionDto
import com.nailorsh.astralis.core.utils.graphics.AstralisTexture
import com.nailorsh.astralis.core.utils.time.currentDate
import com.nailorsh.astralis.core.utils.time.currentTime
import com.nailorsh.astralis.features.home.impl.presentation.data.model.BodyWithPosition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class HomeViewModel(
    private val astralisTexture: (bitmap: ImageBitmap) -> AstralisTexture,
    private val astronomyApi: AstronomyApi
) : BaseViewModel<HomeAction, HomeState>() {
    private val _state = MutableStateFlow(HomeState())
    override val state = _state.asStateFlow()

    init {
        loadPlanets()
    }

    override fun onAction(action: HomeAction) {
        viewModelScope.launch {
            when (action) {
                is HomeAction.OnCameraClicked -> {
                    _state.update { it.copy(isCameraOn = !it.isCameraOn) }
                }

                is HomeAction.OnOrientationTrackingClicked -> {
                    _state.update { it.copy(isOrientationTrackingOn = !it.isOrientationTrackingOn) }
                }
            }
        }
    }

    private fun loadPlanets() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                withContext(AppDispatchers.IO) {
                    val planets = astronomyApi.getAllBodiesPositions(
                        latitude = 55.7522,
                        longitude = 37.6156,
                        elevation = 144.0,
                        fromDate = currentDate(),
                        toDate = currentDate(),
                        time = currentTime()
                    ).getOrThrow()
                        .filter { it.id !in listOf("earth", "moon") }
                        .map { it.toBodyWithPosition() }
                        .also { newPlanets ->
                            newPlanets.forEach {
                                Logger.d("loadPlanets") { "id = ${it.id}" }
                                Logger.d("loadPlanets") { "radiusKm = ${it.radiusKm}" }
                            }
                        }

                    _state.update {
                        it.copy(
                            planets = planets
                        )
                    }
                }
            } catch (e: Exception) {
                Logger.e("loadPlanets", e)
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun BodyPositionDto.toBodyWithPosition(): BodyWithPosition {
        val textureExtension = if (id == "sun") "jpg" else "png"
        val radiusKm =
            if (id == "pluto") 1188.0 else enumValueOf<SolarSystemPlanetsRadius>(id.uppercase()).km


        return BodyWithPosition(
            id = id,
            azimuthDegrees = this.position.horizontal.azimuth.degrees.toDouble(),
            altitudeDegrees = this.position.horizontal.altitude.degrees.toDouble(),
            distanceFromEarthAU = this.distance.fromEarth.au.toDouble(),
            radiusKm = radiusKm,
            texture = astralisTexture(AstralisTexture.getImageBitmapByPath("$id.$textureExtension"))
        )
    }
}