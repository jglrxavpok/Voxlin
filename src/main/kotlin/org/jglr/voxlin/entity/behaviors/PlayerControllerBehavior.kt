package org.jglr.voxlin.entity.behaviors

import org.jglr.voxlin.entity.Behavior
import org.jglr.voxlin.entity.Entity
import org.jglr.voxlin.math.BoundingBox
import org.joml.Vector2f

class PlayerControllerBehavior(entity: Entity) : Behavior(entity) {
    private val LEFT_DIR = -1
    private val RIGHT_DIR = 1
    private var left = false
    private var right = false
    private var direction = RIGHT_DIR
    private var ticksInSameDirection = 0
    private var acceleration = 0f

    override fun update(delta: Float) {
        var targetMovement = 0
        val lastDirection = direction
        if(left) {
            targetMovement -= 1
            direction = LEFT_DIR
        }
        if(right) {
            targetMovement += 1
            direction = RIGHT_DIR
        }

        ticksInSameDirection++

        if(lastDirection != direction) {
            ticksInSameDirection = 0
        }

        if(owner.onGround) {
            acceleration += targetMovement * delta * 6f
        } else {
            acceleration += targetMovement * delta * 4f
        }

        if(acceleration > 1f)
            acceleration = 1f
        else if(acceleration < -1f)
            acceleration = -1f

        acceleration *= 0.98f
        if(owner.onGround && targetMovement == 0) {
            acceleration /= 1.5f // 'friction'
        }

        velocity.x = acceleration * 8f
    }

    fun releaseLeft() {
        left = false
    }

    fun releaseRight() {
        right = false
    }

    fun pressLeft() {
        left = true
    }

    fun pressRight() {
        right = true
    }

    fun jump() {
        if(owner.onGround || owner.ticksSinceOnGround < 2) { // lets a 1/6 seconds to jump after leaving the edge
            val peak = 7.5f // TODO: Change ?
            val gravity = owner.gravityEffect
            val initialSpeed = Math.sqrt(2f*peak*gravity.toDouble())
            velocity.y = initialSpeed.toFloat()
        }
    }
}