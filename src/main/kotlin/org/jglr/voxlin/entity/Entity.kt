package org.jglr.voxlin.entity

import org.jglr.voxlin.level.Level
import org.jglr.voxlin.math.BoundingBox
import org.jglr.voxlin.render.ShaderProgram
import org.jglr.voxlin.utils.Y_AXIS2D
import org.joml.Vector2f
import java.lang.Math.abs
import kotlin.reflect.KClass

abstract class Entity(val level: Level) {

    val behaviors: BehaviorList
    val collisions = HashSet<Entity>()
    val previousCollisions = HashSet<Entity>()
    private var rebuildCollisions = true
    val position = Vector2f()
    val velocity = Vector2f()
    val boundingBox: BoundingBox
    var noclip = false
    var gravityAffected = true
    var gravityEffect = 40f
        private set
    var onGround = true
        private set
    var ticksSinceOnGround = 0
        private set
    var isDead: Boolean = false
        private set
    var health: Int = getMaxHealth()
        private set

    init {
        boundingBox = constructBoundingBox()
        behaviors = BehaviorList(this)
    }

    abstract fun getMaxHealth(): Int

    abstract fun constructBoundingBox(): BoundingBox

    open fun update(delta: Float) {
        onGround = false
        if(gravityAffected) {
            velocity.fma(-delta*gravityEffect, Y_AXIS2D)
        }
        if( ! noclip) {
            handleLevelCollisions(delta)
        }

        ticksSinceOnGround++
        if(onGround) {
            ticksSinceOnGround = 0
        }
        boundingBox.position.set(position)
        behaviors.forEach{ it.update(delta) }
    }

    open fun attack(source: Entity, hitPoints: Int): Boolean {
        health -= hitPoints
        if(health <= 0) {
            isDead = true
        }
        return isDead
    }

    private fun handleLevelCollisions(step: Float) {
        stepXMovement(step)
        stepYMovement(step)
    }

    private fun stepXMovement(step: Float) {
        val startX = if(velocity.x < 0) position.x else position.x + boundingBox.size.x()
        val startTileX = Math.floor(startX.toDouble()).toInt()
        val targetX = startX + step*velocity.x
        val targetTileX = Math.floor(targetX.toDouble()).toInt()

        val lowerTileY = Math.floor(position.y.toDouble()).toInt()
        val upperTileY = Math.floor((position.y+boundingBox.size.y()).toDouble()).toInt()
        val direction = Math.signum(velocity.x).toInt()

        // scan horizontal lines
        var reachedX = targetX
        var collision = false
        for(tileY in lowerTileY..upperTileY) {
            var tileX = startTileX
            while(tileX != targetTileX) {
                tileX += direction
                val tile = level[tileX, tileY]
                if(tile.solid) {
                    if( ! collision || abs(reachedX-startX) > abs(tileX-startX)) {
                        reachedX = tileX.toFloat()
                        reachedX -= direction
                        collision = true
                    }
                }
            }
        }

        if(velocity.x < 0f)
            position.x = reachedX
        else
            position.x = reachedX-boundingBox.size.x()

        if(collision) {
            if(velocity.x > 0)
                position.x += 0.9999f // fixme dirty hack
            velocity.x = 0f
        }
    }

    private fun stepYMovement(step: Float) {
        val startY = if(velocity.y <= 0) position.y else position.y + boundingBox.size.y()
        val startTileY = Math.floor(startY.toDouble()).toInt()
        val targetY = startY + step*velocity.y
        val targetTileY = Math.floor(targetY.toDouble()).toInt()

        val lowerTileX = Math.floor(position.x.toDouble()).toInt()
        val upperTileX = Math.floor((position.x+boundingBox.size.x()).toDouble()).toInt()
        val direction = Math.signum(velocity.y).toInt()

        // scan vertical lines
        var reachedY = targetY
        var collision = false
        for(tileX in lowerTileX..upperTileX) {
            var tileY = startTileY
            while(tileY != targetTileY) {
                tileY += direction
                val tile = level[tileX, tileY]
                if(tile.solid) {
                    if( ! collision || abs(reachedY-startY) > abs(tileY-startY)) {
                        reachedY = (tileY-direction).toFloat()
                        collision = true
                    }
                }
            }
        }

        if(velocity.y < 0f)
            position.y = reachedY
        else
            position.y = reachedY-boundingBox.size.y()

        if(collision) {
            if(velocity.y > 0)
                position.y += 0.9999f // fixme dirty hack

            if(velocity.y < 0f) {
                onGround = true
            }
            velocity.y = 0f
        }
    }

    open fun render(interpolation: Float, shader: ShaderProgram) {

    }

    fun resetCollisions() {
        previousCollisions.clear()
        previousCollisions.addAll(collisions)
        collisions.clear()
        rebuildCollisions = true
    }

    fun needsToRebuildCollisions(): Boolean {
        val result = rebuildCollisions
        rebuildCollisions = false
        return result
    }

    fun addCollision(other: Entity) {
        collisions.add(other)
    }

    inline fun <reified T : Entity> contact(action: (T) -> Unit) = collisions.filter { it is T }.map { it as T }.forEach(action)
    inline fun <reified T : Entity> contactStart(action: (T) -> Unit) = contact<T> {
        if( ! previousCollisions.contains(it)) {
            action.invoke(it)
        }
    }

    infix fun <T : Entity> touches(other: KClass<T>) = collisions.filter { other.isInstance(it) }.count() != 0
    infix fun <T : Entity> startedContact(other: KClass<T>) = previousCollisions.filter { other.isInstance(it) }.count() == 0 && touches(other)
    infix fun touches(other: Entity) = collisions.contains(other)
    infix fun startedContact(other: Entity) = ! previousCollisions.contains(other) && touches(other)
}