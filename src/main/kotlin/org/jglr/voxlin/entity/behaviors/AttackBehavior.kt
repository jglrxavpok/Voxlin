package org.jglr.voxlin.entity.behaviors

import org.jglr.voxlin.entity.Behavior
import org.jglr.voxlin.entity.Entity
import org.jglr.voxlin.math.BoundingBox
import org.joml.Vector2f

abstract class AttackBehavior(owner: Entity): Behavior(owner) {

    protected val alreadyTouched = mutableListOf<Entity>()

    abstract val hitbox: BoundingBox

    abstract fun isAttacking(): Boolean
}