package com.nailorsh.astralis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.decompose.extensions.compose.stack.animation.LocalStackAnimationProvider
import com.nailorsh.astralis.core.di.AppComponent
import com.nailorsh.astralis.core.ui.theme.AppTheme
import com.nailorsh.astralis.core.ui.utils.RepetOnStackAnimationProvider
import com.nailorsh.astralis.features.root.api.LocalRootNavigation
import com.nailorsh.astralis.features.root.api.RootDecomposeComponent

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val rootComponent = AppComponent.appComponent.rootDecomposeComponentFactory(
            defaultComponentContext()
        )

        setContent {
            AndroidApp(rootComponent)
        }
    }
}

@Composable
fun AndroidApp(
    rootComponent: RootDecomposeComponent
) {
    AppTheme {
        CompositionLocalProvider(
            LocalRootNavigation provides rootComponent,
            LocalStackAnimationProvider provides RepetOnStackAnimationProvider
        ) {
            rootComponent.Render(
                Modifier.fillMaxSize()
            )
        }
    }
}