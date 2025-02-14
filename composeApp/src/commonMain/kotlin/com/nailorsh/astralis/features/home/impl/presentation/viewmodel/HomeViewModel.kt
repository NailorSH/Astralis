package com.nailorsh.astralis.features.home.impl.presentation.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import co.touchlab.kermit.Logger
import com.nailorsh.astralis.core.arch.BaseViewModel
import com.nailorsh.astralis.core.coroutines.AppDispatchers
import com.nailorsh.astralis.core.utils.graphics.AstralisTexture
import com.nailorsh.astralis.features.home.impl.presentation.data.model.BodyWithPosition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class HomeViewModel(
    private val astralisTexture: (bitmap: ImageBitmap) -> AstralisTexture
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
            }
        }
    }

    private fun loadPlanets() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                withContext(AppDispatchers.IO) {
                    val planets = listOf(
                        BodyWithPosition(
                            id = "sun",
                            azimuthDegrees = 86.98,
                            altitudeDegrees = -30.30,
                            distanceFromEarthAU = 0.98356,
                            texture = astralisTexture(AstralisTexture.getImageBitmapByPath("sun.jpg"))
                        ),
//                BodyWithPosition(
//                    id = "moon",
//                    azimuthDegrees = 138.79,
//                    altitudeDegrees = 12.93,
//                    distanceFromEarthAU = 0.00269,
//                    textureId = loadTexture(context, R.drawable.moon)
//                ),
                        BodyWithPosition(
                            id = "mercury",
                            azimuthDegrees = 103.87,
                            altitudeDegrees = -14.48,
                            distanceFromEarthAU = 1.01530,
                            texture = astralisTexture(AstralisTexture.getImageBitmapByPath("mercury.png"))
                        ),
                        BodyWithPosition(
                            id = "venus",
                            azimuthDegrees = 29.51,
                            altitudeDegrees = -47.52,
                            distanceFromEarthAU = 0.80160,
                            texture = astralisTexture(AstralisTexture.getImageBitmapByPath("venus.png"))
                        ),
                        BodyWithPosition(
                            id = "mars",
                            azimuthDegrees = 233.92,
                            altitudeDegrees = 47.67,
                            distanceFromEarthAU = 0.67816,
                            texture = astralisTexture(AstralisTexture.getImageBitmapByPath("mars.png"))
                        ),
                        BodyWithPosition(
                            id = "jupiter",
                            azimuthDegrees = 283.23,
                            altitudeDegrees = 17.62,
                            distanceFromEarthAU = 4.14377,
                            texture = astralisTexture(AstralisTexture.getImageBitmapByPath("jupiter.png"))
                        ),
                        BodyWithPosition(
                            id = "saturn",
                            azimuthDegrees = 356.39,
                            altitudeDegrees = -41.98,
                            distanceFromEarthAU = 9.91938,
                            texture = astralisTexture(AstralisTexture.getImageBitmapByPath("saturn.png"))
                        ),
                        BodyWithPosition(
                            id = "uranus",
                            azimuthDegrees = 298.12,
                            altitudeDegrees = 3.83,
                            distanceFromEarthAU = 18.78885,
                            texture = astralisTexture(AstralisTexture.getImageBitmapByPath("uranus.png"))
                        ),
                        BodyWithPosition(
                            id = "neptune",
                            azimuthDegrees = 342.05,
                            altitudeDegrees = -34.85,
                            distanceFromEarthAU = 29.98838,
                            texture = astralisTexture(AstralisTexture.getImageBitmapByPath("neptune.png"))
                        ),
                        BodyWithPosition(
                            id = "pluto",
                            azimuthDegrees = 58.05,
                            altitudeDegrees = -46.08,
                            distanceFromEarthAU = 36.03690,
                            texture = astralisTexture(AstralisTexture.getImageBitmapByPath("pluto.png"))
                        ),
                    )
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
}