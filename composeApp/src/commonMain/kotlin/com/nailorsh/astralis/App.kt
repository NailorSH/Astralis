package com.nailorsh.astralis

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import co.touchlab.kermit.Logger
import com.nailorsh.astralis.core.data.remote.model.AstronomicalBodyDto
import com.nailorsh.astralis.core.di.AppComponent
import com.nailorsh.astralis.core.ui.theme.AppTheme

@Composable
internal fun App() = AppTheme {
    val api = AppComponent.appComponent.astronomyApi

    var list = remember { mutableStateListOf<AstronomicalBodyDto>() }
    LaunchedEffect(Unit) {
        try {
            list = api.getBodies().getOrThrow().toMutableStateList()
        } catch (e: Exception) {
            Logger.e(e) { "App" }
        }
    }

    if (list.isEmpty()) Text("Пусто")
    else {
        LazyColumn {
            items(list) {
                Text("${it.id}, ${it.name}")
            }
        }
    }
}
