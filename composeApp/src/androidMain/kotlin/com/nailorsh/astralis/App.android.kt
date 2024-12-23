package com.nailorsh.astralis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class AppActivity : ComponentActivity() {
    private lateinit var spaceView: SpaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize the GLSurfaceView
        spaceView = SpaceView(this)
        setContentView(spaceView)
    }

    override fun onResume() {
        super.onResume()
        spaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        spaceView.onPause()
    }
}