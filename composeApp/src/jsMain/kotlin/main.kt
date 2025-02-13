import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.stop
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import com.danielgergely.kgl.KglJs
import com.nailorsh.astralis.App
import com.nailorsh.astralis.AstralisAppPlatform
import com.nailorsh.astralis.core.di.PlatformContext
import kotlinx.browser.localStorage
import kotlinx.browser.window
import org.jetbrains.skiko.wasm.onWasmReady
import org.khronos.webgl.WebGLRenderingContext
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.get
import org.w3c.dom.set
import web.dom.DocumentVisibilityState
import web.dom.document
import web.events.EventType
import web.events.addEventListener

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val platform = AstralisAppPlatform()
    platform.start(
        platformContext = object : PlatformContext() {},
        kgl = KglJs(getWebGLContext())
    )

    val lifecycle = LifecycleRegistry()
    val stateKeeper = StateKeeperDispatcher(
        savedState = localStorage[KEY_SAVED_STATE]?.decodeSerializableContainer()
    )
    val root = platform.appComponent.rootDecomposeComponentFactory(
        DefaultComponentContext(lifecycle = lifecycle),
    )

    lifecycle.attachToDocument()

    window.onbeforeunload = {
        localStorage[KEY_SAVED_STATE] = stateKeeper.save().encodeToString()
        null
    }

    onWasmReady {
        CanvasBasedWindow(title = "Astralis") {
            App(root)
        }
    }
}

@Suppress("CAST_NEVER_SUCCEEDS")
fun getWebGLContext(): WebGLRenderingContext {
    val canvas = document.createElement("canvas") as HTMLCanvasElement
    return (canvas.getContext("webgl")
        ?: canvas.getContext("experimental-webgl")) as WebGLRenderingContext
}

private const val KEY_SAVED_STATE = "saved_state"

private fun LifecycleRegistry.attachToDocument() {
    fun onVisibilityChanged() {
        if (document.visibilityState == DocumentVisibilityState.visible) {
            resume()
        } else {
            stop()
        }
    }

    onVisibilityChanged()

    document.addEventListener(
        type = EventType("visibilitychange"),
        handler = { onVisibilityChanged() }
    )
}
