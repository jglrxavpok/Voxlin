package org.jglr.voxlin.render

import org.jglr.voxlin.level.Level
import org.jglr.voxlin.level.TileTextures
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector4f
import org.lwjgl.opengl.GL15
import java.io.File
import java.util.concurrent.LinkedBlockingQueue
import javax.imageio.ImageIO

object RenderingPipeline {

    val WHITE = Vector4f(1f).toImmutable()
    var gameCamera: Camera? = null
    var uiCamera: Camera? = null
    var shader: ShaderProgram? = null

    private val tileBatch = createGameBatch(TileTextures, GL15.GL_DYNAMIC_DRAW)
    private val entityBatch = createGameBatch(MissingTexture, usage = GL15.GL_DYNAMIC_DRAW)
    private val uiBatch = createFixedBatch(MissingTexture, usage = GL15.GL_DYNAMIC_DRAW)

    private val debugUIBatch = createFixedBatch(MissingTexture, usage = GL15.GL_DYNAMIC_DRAW)
    private val debugEntityBatch = createGameBatch(MissingTexture, usage = GL15.GL_DYNAMIC_DRAW)

    private val entitiesPipeline = Channel(entityBatch, "entities")
    private val debugEntitiesPipeline = Channel(debugEntityBatch, "debugEntities")
    private val uiPipeline = Channel(entityBatch, "ui")
    private val debugUIPipeline = Channel(debugEntityBatch, "debugUI")

    /**
     * Creates a batch to be used as a UI batch
     */
    private fun createFixedBatch(texture: Texture, usage: Int): RenderBatch {
        return RenderBatch(texture, usage = usage)
    }

    /**
     * Creates a batch ready to convert game coordinates to pixel coordinates
     */
    private fun createGameBatch(texture: Texture, usage: Int): RenderBatch {
        val result = RenderBatch(texture, usage = usage)
        result.transform(Matrix4f().scaling(Level.TILES_TO_PIXELS.toFloat(), Level.TILES_TO_PIXELS.toFloat(), 0f)) // TODO: zLevel?
        result.pushTransform()
        return result
    }

    fun resetDynamic() {
        fullyResetBatches(entityBatch, uiBatch, debugEntityBatch, debugUIBatch)
    }

    private fun fullyResetBatches(vararg batches: RenderBatch) {
        for(batch in batches) {
            batch.reset()
            batch.currentTransformIdentity() // does not change the scaling
        }
    }

    fun resetTiles() {
        fullyResetBatches(tileBatch)
    }

    fun tile(region: TextureRegion, x: Float, y: Float) {
        val zLevel = 0f // TODO
        val minV = region.maxV
        val maxV = region.minV

        tileBatch.pos(x, y, zLevel).texCoords(region.minU, minV).nonormal().color(WHITE).endVertex()
        tileBatch.pos(x + 1f, y, zLevel).texCoords(region.maxU, minV).nonormal().color(WHITE).endVertex()
        tileBatch.pos(x + 1f, y+1f, zLevel).texCoords(region.maxU, maxV).nonormal().color(WHITE).endVertex()

        tileBatch.pos(x, y, zLevel).texCoords(region.minU, minV).nonormal().color(WHITE).endVertex()
        tileBatch.pos(x, y+1f, zLevel).texCoords(region.minU, maxV).nonormal().color(WHITE).endVertex()
        tileBatch.pos(x + 1f, y+1f, zLevel).texCoords(region.maxU, maxV).nonormal().color(WHITE).endVertex()
    }

    fun uploadTiles() {
        tileBatch.bind()
        tileBatch.upload()
    }

    fun entity(sprite: Sprite) {
        entitiesPipeline.add(InfoPool.get(sprite))
    }

    fun debugEntity(sprite: Sprite) {
        debugEntitiesPipeline.add(InfoPool.get(sprite))
    }

    fun ui(sprite: Sprite) {
        uiPipeline.add(InfoPool.get(sprite))
    }

    fun debugUI(sprite: Sprite) {
        debugUIPipeline.add(InfoPool.get(sprite))
    }

    fun render(shader: ShaderProgram) {
        resetDynamic() // resets dynamic batches
        // uploads dynamic batches

        // order: entities, level, UI, debug
        gameCamera?.update()
        entitiesPipeline.render(shader)

        tileBatch.bind()
        tileBatch.performRenderCall(shader)

        uiCamera?.update()
        uiPipeline.render(shader)


        // debug rendering
        gameCamera?.update()
        debugEntitiesPipeline.render(shader)
        uiCamera?.update()
        debugUIPipeline.render(shader)

        InfoPool.cleanAll()
        entitiesPipeline.clear()
        debugEntitiesPipeline.clear()
    }

    private class Channel(val batch: RenderBatch, val name: String) {
        private val infos = mutableListOf<SpriteInfo>()

        private val regions = hashMapOf<Sprite, Sprite>()
        private val alreadyCachedTextures = mutableSetOf<Texture>()
        private val currentTextureSlots = hashMapOf<Texture, TextureRegion>()
        private var texture = MissingTexture

        fun clear() {
            infos.clear()
        }

        fun add(info: SpriteInfo) {
            infos.add(info)
        }

        fun render(shader: ShaderProgram) {
            batch.bind()
            batch.vertexTransformMatrix.identity()
            batch.texture = getEntitiesTexture()
            for(info in infos) {
                var sprite = regions[info.sprite]
                if(sprite == null) {
                    sprite = createMappedSprite(info.sprite)
                }
                sprite.flipY = info.flipY
                sprite.origin.set(info.origin)
                sprite.setPosition(info.position)
                sprite.angle = info.angle
                sprite.scaleX = info.scaleX
                sprite.scaleY = info.scaleY
                sprite.color.set(info.color)

                sprite.writeToBatch(batch)
            }
            batch.upload()
            batch.performRenderCall(shader)
        }

        /**
         * Creates or updates entity PlayerTexture if needed
         */
        private fun getEntitiesTexture(): Texture {
            val needsRebuild = infos.any { it.sprite.texture !in alreadyCachedTextures }
            if(needsRebuild) {
                alreadyCachedTextures.clear()
                val stitcher = TextureStitcher(MissingImage, false)
                var maxWidth = -1
                var maxHeight = -1
                infos.forEach { (sprite) ->
                    maxWidth = maxOf(sprite.texture.width, maxWidth)
                    maxHeight = maxOf(sprite.texture.height, maxHeight)
                }
                stitcher.tileWidth = maxWidth
                stitcher.tileHeight = maxHeight
                val map = mutableMapOf<Texture, Int>()
                infos.forEach { (sprite) ->
                    if( ! map.containsKey(sprite.texture)) {
                        val id = stitcher.addImage(sprite.texture.source!!, sprite.texture.textureID.toString(), true)
                        map[sprite.texture] = id
                        alreadyCachedTextures.add(sprite.texture)
                    }
                }
                val result = stitcher.stitch()

                ImageIO.write(result, "png", File(".", "run/debug/$name.png"))
                texture = Texture(result)

                // map sprite to a new sprite on the global PlayerTexture
                infos.forEach { (sprite) ->
                    val slot = stitcher.getSlot(map[sprite.texture]!!)
                    currentTextureSlots[sprite.texture] = slot
                    createMappedSprite(sprite)
                }
            }
            return texture
        }

        private fun createMappedSprite(sprite: Sprite): Sprite {
            val slot = currentTextureSlots[sprite.texture]!!
            fun getCoord(spriteUV: Vector2f): Vector2f {
                val slotMinU: Float = slot.minU
                val slotMinV: Float = slot.minV
                val slotMaxU: Float = slot.maxU
                val slotMaxV: Float = slot.maxV
                val u = (spriteUV.x() * sprite.texture.width.toFloat() / texture.width) * (slotMaxU-slotMinU) + slotMinU
                val v = (spriteUV.y() * sprite.texture.height.toFloat() / texture.height) * (slotMaxV-slotMinV) + slotMinV
                return Vector2f(u, v)
            }
            val region = Sprite(texture, getCoord(sprite.minUV), getCoord(sprite.maxUV))
            regions[sprite] = region
            return region
        }
    }
}

object InfoPool {

    private val available = LinkedBlockingQueue<SpriteInfo>()
    private val used = mutableListOf<SpriteInfo>()

    fun get(sprite: Sprite): SpriteInfo {
        val info = if(available.isNotEmpty()) available.take() else SpriteInfo(sprite)

        info.sprite = sprite
        info.color.set(sprite.color)
        info.scaleX = sprite.scaleX
        info.scaleY = sprite.scaleY
        info.position.set(sprite.immutablePosition)
        info.origin.set(sprite.origin)
        info.angle = sprite.angle
        info.flipY = sprite.flipY

        used.add(info)
        return info
    }

    fun cleanAll() {
        available.addAll(used)
        used.clear()
    }
}

data class SpriteInfo(var sprite: Sprite) {
    val position: Vector2f = Vector2f()
    val color: Vector4f = Vector4f(1f)
    var scaleX: Float = 1f
    var scaleY: Float = 1f
    var angle: Float = 0f
    val origin: Vector2f = Vector2f()
    var flipY: Boolean = false
}