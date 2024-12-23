package com.nailorsh.astralis

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import co.touchlab.kermit.Logger
import com.nailorsh.astralis.core.data.remote.model.BodyPositionDto
import com.nailorsh.astralis.core.di.AppComponent
import com.nailorsh.astralis.core.ui.theme.AppTheme
import com.nailorsh.astralis.core.utils.time.currentDate
import com.nailorsh.astralis.core.utils.time.currentTime

@Composable
internal fun App() = AppTheme {
    val api = AppComponent.appComponent.astronomyApi

    val list = remember { mutableStateListOf<BodyPositionDto>() }
    LaunchedEffect(Unit) {
        try {
            list.addAll(
                api.getAllBodiesPositions(
                    latitude = 55.7522,
                    longitude = 37.6156,
                    elevation = 144.0,
                    fromDate = currentDate(),
                    toDate = currentDate(),
                    time = currentTime(),
                ).getOrThrow().toMutableStateList()
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
                    Text("Planet: ${it.name}")
                    Text("altitude: ${it.position.horizontal.altitude}")
                    Text("azimuth: ${it.position.horizontal.azimuth}")
                    Text("distance in au: ${it.distance.fromEarth.au}")
                    Text("distance in km: ${it.distance.fromEarth.km}")
                    HorizontalDivider()
                }
            }
        }
    }
}