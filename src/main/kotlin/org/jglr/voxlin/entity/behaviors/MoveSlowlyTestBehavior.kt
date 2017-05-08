package org.jglr.voxlin.entity.behaviors

import org.jglr.voxlin.entity.Behavior
import org.jglr.voxlin.entity.Entity

class MoveSlowlyTestBehavior(entity: Entity) : Behavior(entity) {
    override fun update(delta: Float) {
        val speed = 2f
        velocity.x = speed

        if(position.x() >= 500f)
            position.x = 0f
    }
}