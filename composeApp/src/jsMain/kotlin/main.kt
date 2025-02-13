import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.nailorsh.astralis.core.ui.theme.AppTheme
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.skiko.wasm.onWasmReady

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        CanvasBasedWindow(title = "Astralis") {
            AppTheme {
                Text("Hello Astralis")
            }
        }
    }
}
