package org.jglr.voxlin.entity

import org.jglr.voxlin.entity.behaviors.PlayerAttackCoordinator
import org.jglr.voxlin.level.Level
import org.jglr.voxlin.math.BoundingBox
import org.jglr.voxlin.render.*
import org.jglr.voxlin.utils.ResourceLocation
import org.joml.Vector2f
import java.lang.Math.abs

val PlayerTexture = Texture(ResourceLocation("textures/player_walk.png"))
val DebugRedTexture = Texture(ResourceLocation("textures/red.png"))

class PlayerEntity(lvl: Level) : Entity(lvl) {
    override fun getMaxHealth(): Int = 10

    override fun constructBoundingBox(): BoundingBox = BoundingBox(Vector2f(1f, 2f))

    val idleSprite = Sprite(PlayerTexture, Vector2f(), Vector2f(0.5f, 1f))
    val walkSprite = Sprite(PlayerTexture, Vector2f(0.5f, 0f), Vector2f(1f))
    val hitboxSprite = Sprite(DebugRedTexture, Vector2f(), Vector2f(1f))
    private var frame = 0
    private var invulnerabilityFrames = 0

    override fun update(delta: Float) {
        super.update(delta)
        if(invulnerabilityFrames > 0)
            invulnerabilityFrames--
    }

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

        if(invulnerabilityFrames > 0) {
            val duration = 5
            if(invulnerabilityFrames % (duration*2) >= duration) {
                val rgb = 1.5f
                sprite.color.set(rgb, rgb, rgb, 1f)
            } else {
                sprite.color.set(1f)
            }
        } else {
            sprite.color.set(1f)
        }
        sprite.setPosition(pos)
        RenderingPipeline.entity(sprite)

        if(behaviors.has<PlayerAttackCoordinator>()) {
            behaviors.get<PlayerAttackCoordinator>().let {
                if (it.isAttacking()) {
                    val attack = it.getCurrentAttack()!!
                    hitboxSprite.color.w = 0.5f
                    hitboxSprite.scaleX = attack.hitbox.size.x()
                    hitboxSprite.scaleY = attack.hitbox.size.y()
                    hitboxSprite.setPosition(attack.hitbox.position)

                    RenderingPipeline.debugEntity(hitboxSprite)
                }
            }
        }
    }

    override fun attack(source: Entity, hitPoints: Int): Boolean {
        if(invulnerabilityFrames <= 0) {
            invulnerabilityFrames = 25
            return super.attack(source, hitPoints)
        }
        return false
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