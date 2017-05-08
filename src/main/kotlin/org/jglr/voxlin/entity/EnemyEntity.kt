package org.jglr.voxlin.entity

import org.jglr.voxlin.entity.behaviors.PlayerAttackCoordinator
import org.jglr.voxlin.level.Level
import org.jglr.voxlin.math.BoundingBox
import org.jglr.voxlin.render.*
import org.jglr.voxlin.utils.ResourceLocation
import org.joml.Vector2f
import java.lang.Math.abs

val EnemyTestTexture = Texture(ResourceLocation("textures/player_walk.png"))

class EnemyEntity(lvl: Level) : Entity(lvl) {
    override fun getMaxHealth(): Int = 10

    override fun constructBoundingBox(): BoundingBox = BoundingBox(Vector2f(1f, 2f))

    val idleSprite = Sprite(PlayerTexture, Vector2f(), Vector2f(0.5f, 1f))
    val walkSprite = Sprite(PlayerTexture, Vector2f(0.5f, 0f), Vector2f(1f))
    private var frame = 0
    private var damageFrames = 0
    private var damageIntensity = 0

    override fun render(interpolation: Float, shader: ShaderProgram) {
        val pos = Vector2f(position)
        frame++
        if(frame > 60)
            frame = 0
        val sprite = getCorrectSprite(frame)
        sprite.scaleX = boundingBox.size.x() / sprite.width *2f
        sprite.scaleY = boundingBox.size.y() / sprite.height *2f
        if(velocity.x < 0f) {
            sprite.scaleX *= -1f
            pos.add(-sprite.scaledWidth, 0f)
            pos.add(-2f / Level.TILES_TO_PIXELS.toFloat() *4f, 0f)
        } else {
            pos.add(-7f / Level.TILES_TO_PIXELS.toFloat() *4f, 0f)
        }
        sprite.setPosition(pos)
        if(damageFrames > 0) {
            val duration = 9
            if(damageFrames % (duration*2) >= duration) {
                sprite.color.y = 1f-damageIntensity*.5f // green
                sprite.color.z = 1f-damageIntensity*.5f // blue
            } else {
                sprite.color.y = 1f
                sprite.color.z = 1f
            }
            damageFrames--
        } else {
            sprite.color.y = 1f
            sprite.color.z = 1f
        }
        RenderingPipeline.entity(sprite)
    }

    override fun attack(source: Entity, hitPoints: Int): Boolean {
        damageFrames = 50
        damageIntensity = hitPoints
        return super.attack(source, hitPoints)
    }

    private fun getCorrectSprite(frame: Int): Sprite {
        val cyclesPerSecond = 10
        if(onGround && abs(velocity.x) > 0.01f) {
            if((frame / cyclesPerSecond) % 2 == 0) {
                return idleSprite
            }
            return walkSprite
        }
        return idleSprite
    }
}