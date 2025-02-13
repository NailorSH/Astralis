package com.nailorsh.astralis

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.nailorsh.astralis.core.utils.graphics.AstralisTexture
import com.nailorsh.astralis.core.utils.graphics.getImageBitmapByPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Заглушка вместо spaceView
        val placeholderView = FrameLayout(this).apply { id = View.generateViewId() }
        setContentView(placeholderView)

        // Запускаем корутину для загрузки планет
        lifecycleScope.launch {
            val planets = loadPlanets() // suspend-функция
            setupView(planets) // Передаём данные во View
        }
    }

    private fun setupView(planets: List<BodyWithPosition>) {
        val spaceView = SpaceView(this, planets)
        setContentView(spaceView)
    }

    // Пример suspend-функции загрузки данных
    private suspend fun loadPlanets(): List<BodyWithPosition> {
        return withContext(Dispatchers.IO) {
            listOf(
                BodyWithPosition(
                    id = "sun",
                    azimuthDegrees = 86.98,
                    altitudeDegrees = -30.30,
                    distanceFromEarthAU = 0.98356,
                    texture = AstralisTexture(getImageBitmapByPath("sun.jpg"))
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
                    texture = AstralisTexture(getImageBitmapByPath("mercury.png"))
                ),
                BodyWithPosition(
                    id = "venus",
                    azimuthDegrees = 29.51,
                    altitudeDegrees = -47.52,
                    distanceFromEarthAU = 0.80160,
                    texture = AstralisTexture(getImageBitmapByPath("venus.png"))
                ),
                BodyWithPosition(
                    id = "mars",
                    azimuthDegrees = 233.92,
                    altitudeDegrees = 47.67,
                    distanceFromEarthAU = 0.67816,
                    texture = AstralisTexture(getImageBitmapByPath("mars.png"))
                ),
                BodyWithPosition(
                    id = "jupiter",
                    azimuthDegrees = 283.23,
                    altitudeDegrees = 17.62,
                    distanceFromEarthAU = 4.14377,
                    texture = AstralisTexture(getImageBitmapByPath("jupiter.png"))
                ),
                BodyWithPosition(
                    id = "saturn",
                    azimuthDegrees = 356.39,
                    altitudeDegrees = -41.98,
                    distanceFromEarthAU = 9.91938,
                    texture = AstralisTexture(getImageBitmapByPath("saturn.png"))
                ),
                BodyWithPosition(
                    id = "uranus",
                    azimuthDegrees = 298.12,
                    altitudeDegrees = 3.83,
                    distanceFromEarthAU = 18.78885,
                    texture = AstralisTexture(getImageBitmapByPath("uranus.png"))
                ),
                BodyWithPosition(
                    id = "neptune",
                    azimuthDegrees = 342.05,
                    altitudeDegrees = -34.85,
                    distanceFromEarthAU = 29.98838,
                    texture = AstralisTexture(getImageBitmapByPath("neptune.png"))
                ),
                BodyWithPosition(
                    id = "pluto",
                    azimuthDegrees = 58.05,
                    altitudeDegrees = -46.08,
                    distanceFromEarthAU = 36.03690,
                    texture = AstralisTexture(getImageBitmapByPath("pluto.png"))
                ),
            )
        }
    }
}

@Composable
fun Hello(modifier: Modifier = Modifier) {

}