package org.jglr.voxlin.entity

import org.joml.Vector2f
import org.joml.Vector2fc
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

abstract class Behavior(val owner: Entity) {
    abstract fun update(delta: Float)

    val position = owner.position
    val velocity = owner.velocity
    val boundingBox = owner.boundingBox

    inline fun <reified T : Entity> contact(action: (T) -> Unit) = owner.contact(action)
    inline fun <reified T : Entity> contactStart(action: (T) -> Unit) = owner.contactStart(action)
}
