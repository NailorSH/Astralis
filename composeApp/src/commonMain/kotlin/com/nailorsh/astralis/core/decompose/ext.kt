package com.nailorsh.astralis.core.decompose

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigator
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.popTo
import com.arkivanov.decompose.value.Value
import kotlin.reflect.KClass

inline fun <C : Any> StackNavigator<C>.popToRoot(
    crossinline onComplete: (isSuccess: Boolean) -> Unit = {},
) = popTo(0, onComplete)

fun <C : Any, T : Any> Value<ChildStack<C, T>>.findComponentByConfig(configClazz: KClass<out C>): T? {
    return value.items.find {
        it.configuration::class == configClazz
    }?.instance
}

inline fun <C : Any> StackNavigator<C>.popOr(
    crossinline fallback: () -> Unit = {},
) = pop { onComplete ->
    if (!onComplete) {
        fallback()
    }
}