package org.jglr.voxlin.render

import org.jglr.voxlin.Transform
import org.jglr.voxlin.Transformable

class Animation(vararg val sprites: Sprite) : Transformable {
    private val transform = Transform()
    private val defaultBatch = RenderBatch(sprites[0].texture)

    override fun transform(): Transform = transform

    fun render(batch: RenderBatch = defaultBatch) {
        val sprite = selectSprite()

    }

    private fun selectSprite(): Sprite {
        return sprites[0] // TODO
    }
}