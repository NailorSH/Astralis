package com.nailorsh.astralis

import android.os.Bundle
import androidx.activity.ComponentActivity

class AppActivity : ComponentActivity() {
    private lateinit var planetVisualizationView: PlanetVisualizationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the GLSurfaceView
        planetVisualizationView = PlanetVisualizationView(this)
        setContentView(planetVisualizationView)
    }

    override fun onResume() {
        super.onResume()
        planetVisualizationView.onResume()
    }

    override fun onPause() {
        super.onPause()
        planetVisualizationView.onPause()
    }
}