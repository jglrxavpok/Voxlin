package org.jglr.voxlin.render

import org.jglr.voxlin.utils.ResourceLocation
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL12
import java.io.IOException
import java.nio.IntBuffer
import javax.imageio.ImageIO

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_BGRA
import java.awt.image.BufferedImage

class Texture {
    var textureID: Int = 0
        private set

    var width: Int = 0
        private set
    var height: Int = 0
        private set
    var source: BufferedImage? = null
        private set

    constructor(location: ResourceLocation) {
        try {
            val image = ImageIO.read(location.newInputStream())
            source = image
            val pixels = image.getRGB(0, 0, image.width, image.height, null, 0, image.width)
            val pixelBuffer = BufferUtils.createIntBuffer(pixels.size)
            pixelBuffer.put(pixels)

            init(image.width, image.height, pixelBuffer)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    constructor(image: BufferedImage) {
        source = image
        val pixels = image.getRGB(0, 0, image.width, image.height, null, 0, image.width)
        val pixelBuffer = BufferUtils.createIntBuffer(pixels.size)
        pixelBuffer.put(pixels)

        init(image.width, image.height, pixelBuffer)
    }

    private fun init(width: Int, height: Int, pixels: IntBuffer) {
        this.width = width
        this.height = height
        textureID = glGenTextures()
        bind()
        glTexParameteri(GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0)
        glTexParameteri(GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

        pixels.flip()
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_BGRA, GL_UNSIGNED_BYTE, pixels)
        unbind()
    }

    fun bind() {
        glBindTexture(GL_TEXTURE_2D, textureID)
    }

    fun unbind() {
        glBindTexture(GL_TEXTURE_2D, 0)
    }

}

val MissingImage = BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB).apply {
    setRGB(0, 0, 0xFF7F006E.toInt())
    setRGB(1, 1, 0xFF7F006E.toInt())
    setRGB(0, 1, 0xFF000000.toInt())
    setRGB(1, 0, 0xFF000000.toInt())
}
val MissingTexture = Texture(MissingImage)
