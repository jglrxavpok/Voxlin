package org.jglr.voxlin.level

import org.jglr.voxlin.render.*
import org.jglr.voxlin.utils.ResourceLocation
import java.io.File
import javax.imageio.ImageIO

val TileDictionary = mutableMapOf<Int, Tile>()
val TileNameDictionary = mutableMapOf<String, Tile>()

abstract class Tile(val idText: String, val id: Int) {

    var tilesetArea: TextureRegion = TextureRegion(0f, 0f, 1f, 1f, Level.TILES_TO_PIXELS, Level.TILES_TO_PIXELS)

    init {
        TileDictionary.put(id, this)
        TileNameDictionary.put(idText, this)
    }

    abstract val solid: Boolean

}

val Air = object : Tile("air", 0) {
    override val solid: Boolean = false
}

val Ground = object : Tile("ground", 1) {
    override val solid: Boolean = true
}

val OutOfBounds = object : Tile("out", -1) {
    override val solid: Boolean = true
}

val Invalid = object: Tile("missigno", -2) {
    override val solid: Boolean
        get() = throw IllegalAccessException("Trying to access properties of invalid tile")
}

val TileTextures: Texture by lazy {
    val stitcher = TextureStitcher(MissingImage, putInCorner = false)
    stitcher.tileWidth = Level.TILES_TO_PIXELS * 4
    stitcher.tileHeight = Level.TILES_TO_PIXELS * 4
    val indexes = mutableMapOf<Tile, Int>()
    TileDictionary.values.forEach {
        try {
            val tileImage = ImageIO.read(ResourceLocation("textures/tiles/${it.idText}.png").newInputStream())
            indexes.put(it, stitcher.addImage(tileImage, it.idText, forceResize = true))
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            indexes.put(it, stitcher.addImage(MissingImage, it.idText, forceResize = true))
        }
    }
    val stitchedImage = stitcher.stitch()
    ImageIO.write(stitchedImage, "png", File(".", "run/debug/tiles.png"))
    indexes.keys.forEach {
        it.tilesetArea = stitcher.getSlot(indexes[it]!!)
    }
    val stitchedTexture = Texture(stitchedImage)
    return@lazy stitchedTexture
}