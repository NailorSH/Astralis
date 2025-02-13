package com.nailorsh.astralis.features.home.impl.api

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.nailorsh.astralis.core.arch.viewModelWithFactory
import com.nailorsh.astralis.core.decompose.DecomposeOnBackParameter
import com.nailorsh.astralis.features.home.api.HomeScreenDecomposeComponent
import com.nailorsh.astralis.features.home.impl.presentation.CreateHomeScreen
import com.nailorsh.astralis.features.home.impl.presentation.viewmodel.MainViewModel
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding

@Inject
class HomeScreenDecomposeComponentImpl(
    @Assisted componentContext: ComponentContext,
    @Assisted private val onBackParameter: DecomposeOnBackParameter,
    private val mainViewModel: MainViewModel
) : HomeScreenDecomposeComponent(componentContext) {
    @Composable
    override fun Render() {
        val viewModel = viewModelWithFactory(null) {
            mainViewModel
        }

        CreateHomeScreen(
            viewModel = viewModel
        )
    }

    @Inject
    @ContributesBinding(AppScope::class, HomeScreenDecomposeComponent.Factory::class)
    class Factory(
        private val factory: (
            componentContext: ComponentContext,
            onBackParameter: DecomposeOnBackParameter
        ) -> HomeScreenDecomposeComponentImpl
    ) : HomeScreenDecomposeComponent.Factory {
        override fun invoke(
            componentContext: ComponentContext,
            onBackParameter: DecomposeOnBackParameter
        ) = factory(componentContext, onBackParameter)
    }
}