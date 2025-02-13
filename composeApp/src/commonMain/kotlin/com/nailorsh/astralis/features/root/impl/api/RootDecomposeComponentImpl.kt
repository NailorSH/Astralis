package com.nailorsh.astralis.features.root.impl.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushToFront
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import com.nailorsh.astralis.core.decompose.DecomposeComponent
import com.nailorsh.astralis.core.decompose.popOr
import com.nailorsh.astralis.features.home.api.HomeScreenDecomposeComponent
import com.nailorsh.astralis.features.root.api.RootDecomposeComponent
import com.nailorsh.astralis.features.root.api.model.RootScreenConfig
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding

@Inject
class RootDecomposeComponentImpl(
    @Assisted componentContext: ComponentContext,
    private val homeScreenDecomposeComponentFactory: HomeScreenDecomposeComponent.Factory,
) : RootDecomposeComponent, ComponentContext by componentContext {
    private val navigation = StackNavigation<RootScreenConfig>()
    private val stack: Value<ChildStack<RootScreenConfig, DecomposeComponent>> = childStack(
        source = navigation,
        serializer = RootScreenConfig.serializer(),
        initialStack = { listOf(RootScreenConfig.Home) },
        handleBackButton = true,
        childFactory = ::child
    )

    @Composable
    override fun Render(modifier: Modifier) {
        val childStack by stack.subscribeAsState()

        Children(modifier = modifier, stack = childStack) {
            it.instance.Render()
        }
    }

    override fun push(config: RootScreenConfig) {
        navigation.pushToFront(config)
    }

    private fun child(
        rootScreenConfig: RootScreenConfig,
        componentContext: ComponentContext
    ): DecomposeComponent {
        return when (rootScreenConfig) {
            is RootScreenConfig.Home -> homeScreenDecomposeComponentFactory(
                componentContext,
                navigation::pop
            )
        }
    }

    private fun internalOnBack() {
        navigation.popOr()
    }

    private fun navigateToHome() {
        navigation.replaceCurrent(RootScreenConfig.Home)
    }

    @Inject
    @ContributesBinding(AppScope::class, RootDecomposeComponent.Factory::class)
    class Factory(
        private val factory: (
            componentContext: ComponentContext,
        ) -> RootDecomposeComponentImpl
    ) : RootDecomposeComponent.Factory {
        override fun invoke(
            componentContext: ComponentContext,
        ) = factory(componentContext)
    }
}