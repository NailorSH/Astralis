import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.danielgergely.kgl.KglLwjgl
import com.nailorsh.astralis.App
import com.nailorsh.astralis.AstralisAppPlatform
import com.nailorsh.astralis.core.di.PlatformContext
import java.awt.Dimension

fun main() = application {
    val platform = AstralisAppPlatform()
    platform.start(
        platformContext = object : PlatformContext() {},
        kgl = KglLwjgl
    )

    Window(
        title = "Astralis",
        state = rememberWindowState(width = 800.dp, height = 600.dp),
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(350, 600)
        App()
    }
}

@Preview
@Composable
fun AppPreview() { App() }