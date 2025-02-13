package com.nailorsh.astralis.core.utils.graphics

import androidx.compose.ui.graphics.ImageBitmap
import com.danielgergely.kgl.GL_CLAMP_TO_EDGE
import com.danielgergely.kgl.GL_LINEAR
import com.danielgergely.kgl.GL_TEXTURE_2D
import com.danielgergely.kgl.GL_TEXTURE_MAG_FILTER
import com.danielgergely.kgl.GL_TEXTURE_MIN_FILTER
import com.danielgergely.kgl.GL_TEXTURE_WRAP_S
import com.danielgergely.kgl.GL_TEXTURE_WRAP_T
import com.danielgergely.kgl.Kgl
import com.danielgergely.kgl.Texture
import com.nailorsh.astralis.core.utils.getImageBitmap
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

const val TEXTURES_PATH = "files/textures"

@Inject
class AstralisTexture(
    @Assisted private val bitmap: ImageBitmap,
    private val kgl: Kgl
) {
    var id: Texture? = null

    fun load() {
        val textureHandle = kgl.createTexture()

        if (!textureHandle.equals(0)) {
            kgl.bindTexture(GL_TEXTURE_2D, textureHandle)

            kgl.texParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            kgl.texParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            kgl.texParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            kgl.texParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

            kgl.texImage2D(GL_TEXTURE_2D, 0, bitmap)
            kgl.bindTexture(GL_TEXTURE_2D, null)
        }

        id = textureHandle
    }

    companion object {
        suspend fun getImageBitmapByPath(path: String): ImageBitmap {
            return getImageBitmap("$TEXTURES_PATH/$path")
        }
    }
}

expect fun Kgl.texImage2D(target: Int, level: Int, bitmap: ImageBitmap, border: Int = 0)