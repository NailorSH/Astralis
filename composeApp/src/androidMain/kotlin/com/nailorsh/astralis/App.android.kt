package com.nailorsh.astralis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext
import com.nailorsh.astralis.core.di.AppComponent

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val rootComponent = AppComponent.appComponent.rootDecomposeComponentFactory(
            defaultComponentContext()
        )

        setContent {
            App(rootComponent)
        }
    }
}