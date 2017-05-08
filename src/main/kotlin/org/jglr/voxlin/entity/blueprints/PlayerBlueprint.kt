package org.jglr.voxlin.entity.blueprints

import org.jglr.voxlin.entity.Behavior
import org.jglr.voxlin.entity.Entity
import org.jglr.voxlin.entity.behaviors.MeleeAttackBehavior
import org.jglr.voxlin.entity.behaviors.PlayerAttackCoordinator
import org.jglr.voxlin.entity.behaviors.PlayerControllerBehavior
import org.jglr.voxlin.entity.behaviors.RangeAttackBehavior

object PlayerBlueprint: Blueprint() {
    override val staticBehaviors: List<(Entity) -> Behavior> = listOf(::PlayerControllerBehavior, ::PlayerAttackCoordinator, ::MeleeAttackBehavior, ::RangeAttackBehavior)
}