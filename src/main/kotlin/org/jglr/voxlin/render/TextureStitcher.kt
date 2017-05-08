package org.jglr.voxlin.render

import java.awt.image.BufferedImage

class TextureStitcher(private var emptySlotImage: BufferedImage?, private val putInCorner: Boolean) {
    private val imgs: MutableList<BufferedImage> = mutableListOf()
    private val textureRegions: MutableList<TextureRegion> = mutableListOf()
    /**
     * Gets tile width
     */
    /**
     * Sets tile width
     */
    var tileWidth: Int = -1
    /**
     * Gets tile height
     */
    /**
     * Sets tile height
     */
    var tileHeight: Int = -1

    /**
     * Adds a image to the list and resizes it if asked
     */
    fun addImage(img: BufferedImage, name: String, forceResize: Boolean = false): Int {
        var image = img
        if (tileWidth == -1 || tileHeight == -1) {
            tileWidth = image.width
            tileHeight = image.height
        } else if (image.width != tileWidth || image.height != tileHeight) {
            if (!forceResize && !putInCorner) {
                error("Unexpected size in " + name + ": " + image.width + "x" + image.height + "px, expected " + tileWidth + "x" + tileHeight + "px. Image index: " + imgs.size)
            } else if (forceResize) {
                image = resize(image, tileWidth, tileHeight)
            }
        }
        imgs.add(image)
        return imgs.size - 1
    }

    private fun resize(image: BufferedImage?, width: Int, height: Int): BufferedImage {
        val result = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics = result.createGraphics()
        graphics.drawImage(image, 0, 0, width, height, null)
        graphics.dispose()
        return result
    }

    /**
     * Creates a big BufferImage containing all previously given images
     */
    fun stitch(): BufferedImage {

        var nbrY = upperPowerOf2(Math.floor(Math.sqrt(imgs.size.toDouble())).toInt())
        val nbrX = Math.ceil(imgs.size.toDouble() / nbrY.toDouble()).toInt()

        while (nbrX * nbrY - (imgs.size - 1) > nbrY)
            nbrY--
        var width = nbrX * tileWidth
        var height = nbrY * tileHeight
        if (height < tileHeight)
            height = tileHeight
        if (width < tileWidth)
            width = tileWidth
        val result = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = result.createGraphics()
        for (i in imgs.indices) {
            val column = i % nbrX
            val row = i / nbrX
            val x = column * tileWidth
            val y = row * tileHeight
            g.drawImage(imgs[i], column * tileWidth, row * tileHeight, null)
            textureRegions.add(TextureRegion(x.toFloat() / width.toFloat(), y.toFloat() / height.toFloat(), (x + tileWidth).toFloat() / width.toFloat(), (y + tileHeight).toFloat() / height.toFloat(), width, height))
        }

        emptySlotImage = resize(emptySlotImage, tileWidth, tileHeight)
        for (n in imgs.size..nbrX * nbrY - 1) {
            val column = n % nbrX
            val row = n / nbrX
            g.drawImage(emptySlotImage, column * tileWidth, row * tileHeight, null)
        }
        g.dispose()
        return result
    }

    /**
     * From http://graphics.stanford.edu/~seander/bithacks.html
     */
    fun upperPowerOf2(v: Int): Int {
        var value = v
        value--
        value = value or (value shr 1)
        value = value or (value shr 2)
        value = value or (value shr 4)
        value = value or (value shr 8)
        value = value or (value shr 16)
        value = value or (value shr 32)
        value++
        return value
    }

    /**
     * Gets min U coordinate for given index
     */
    fun getMinU(index: Int): Float {
        return textureRegions[index].minU
    }

    /**
     * Gets min V coordinate for given index
     */
    fun getMinV(index: Int): Float {
        return textureRegions[index].minV
    }

    /**
     * Gets max U coordinate for given index
     */
    fun getMaxU(index: Int): Float {
        return textureRegions[index].maxU
    }

    /**
     * Gets max V coordinate for given index
     */
    fun getMaxV(index: Int): Float {
        return textureRegions[index].maxV
    }

    /**
     * Gets width for given index
     */
    fun getWidth(index: Int): Int {
        return textureRegions[index].width
    }

    /**
     * Gets height for given index
     */
    fun getHeight(index: Int): Int {
        return textureRegions[index].height
    }

    fun getSlot(index: Int): TextureRegion {
        return textureRegions[index]
    }
}

class TextureRegion constructor(var minU: Float, var minV: Float, var maxU: Float, var maxV: Float, var width: Int = 16, var height: Int = 16)