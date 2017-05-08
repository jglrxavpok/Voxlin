package org.jglr.voxlin.render

import org.jglr.voxlin.Transform
import org.jglr.voxlin.Transformable
import org.jglr.voxlin.utils.Z_AXIS
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector4f

class Sprite(val texture: Texture, val minUV: Vector2f, val maxUV: Vector2f) : Transformable {
    private val transform = Transform()
    val width: Float = texture.width * (maxUV.x - minUV.x)
    val height: Float = texture.height * (maxUV.y - minUV.y)
    val origin: Vector2f = Vector2f()
    private val position: Vector2f = Vector2f()
    val immutablePosition: Vector2fc = position.toImmutable()
    val color: Vector4f = Vector4f(1f, 1f, 1f, 1f)
    var scaleX: Float = 1f
    var scaleY: Float = 1f
    var angle: Float = 0f
        set(angle) {
            field = angle
            updateTransform()
        }
    var flipY = true

    override fun transform(): Transform = transform

    fun setPosition(pos: Vector2f) {
        setPosition(pos.x, pos.y)
    }

    fun setPosition(x: Float, y: Float) {
        position.set(x, y)
        updateTransform()
    }

    private fun updateTransform() {
        transform.translation(position.x(), position.y(), 0f)
                .rotate(this.angle, 0f, 0f, 1f)
    }

    fun writeToBatch(batch: RenderBatch) {
        if (batch.texture !== texture) {
            throw RuntimeException("Cannot render a sprite to a batch that does not use the same PlayerTexture")
        }
        updateTransform()
        batch.vertexTransformation(transform.toMatrix())

        val minV = if(flipY) maxUV.y else minUV.y
        val maxV = if(flipY) minUV.y else maxUV.y
        batch.pos(-origin.x, -origin.y, 0f).texCoords(minUV.x, minV).normal(0f, 0f, 0f).color(color).endVertex()
        batch.pos(-origin.x + width * scaleX, -origin.y, 0f).texCoords(maxUV.x, minV).normal(0f, 0f, 0f).color(color).endVertex()
        batch.pos(-origin.x + width * scaleX, -origin.y + height * scaleY, 0f).texCoords(maxUV.x, maxV).normal(0f, 0f, 0f).color(color).endVertex()

        batch.pos(-origin.x, -origin.y, 0f).texCoords(minUV.x, minV).normal(0f, 0f, 0f).color(color).endVertex()
        batch.pos(-origin.x, -origin.y + height * scaleY, 0f).texCoords(minUV.x, maxV).normal(0f, 0f, 0f).color(color).endVertex()
        batch.pos(-origin.x + width * scaleX, -origin.y + height * scaleY, 0f).texCoords(maxUV.x, maxV).normal(0f, 0f, 0f).color(color).endVertex()
    }

    val scaledHeight: Float
        get() = height * scaleY

    val scaledWidth: Float
        get() = width * scaleX

}
