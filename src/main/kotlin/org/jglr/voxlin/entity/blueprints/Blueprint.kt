package org.jglr.voxlin.entity.blueprints

import org.jglr.voxlin.entity.Behavior
import org.jglr.voxlin.entity.Entity
import org.jglr.voxlin.entity.behaviors.PlayerControllerBehavior
import java.util.function.Function
import kotlin.reflect.KFunction

abstract class Blueprint {


    abstract val staticBehaviors: List<(Entity) -> Behavior>

    fun apply(entity: Entity) {
        for(staticComponent in staticBehaviors) {
            entity.behaviors.add(staticComponent(entity))
        }
    }
}