package org.jglr.voxlin.entity.behaviors

import org.jglr.voxlin.entity.Behavior
import org.jglr.voxlin.entity.Entity
import org.jglr.voxlin.entity.PlayerEntity

class ResetPositionOnContactTestBehavior(entity: Entity) : Behavior(entity) {
    override fun update(delta: Float) {
        contact { _: PlayerEntity ->
            position.set(1f, 10f)
            velocity.set(0f, 0f)
        }
    }
}