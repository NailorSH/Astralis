package com.nailorsh.astralis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.nailorsh.astralis.core.data.local.model.PlanetData
import com.nailorsh.astralis.core.di.AppComponent
import com.nailorsh.astralis.core.ui.theme.AppTheme

@Composable
internal fun App() = AppTheme {
    val api = AppComponent.appComponent.celestialDataRepository

    val list = remember { mutableStateListOf<PlanetData>() }
    LaunchedEffect(Unit) {
        try {
            list.addAll(
                api.getPlanets().values
            )
        } catch (e: Exception) {
            Logger.e(e) { "App" }
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        if (list.toList().isEmpty()) Text("Пусто")
        else {
            LazyColumn {
                items(list.toList()) {
                    Row {
                        Column {
                            Text("Planet: ${it.name}")
                            Text("type: ${it.type}")
                            Text("massKg: ${it.massKg}")
                        }
                        Box(
                            modifier = Modifier.size(20.dp)
                                .background(color = it.getColorRGB() ?: Color.Transparent)
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}