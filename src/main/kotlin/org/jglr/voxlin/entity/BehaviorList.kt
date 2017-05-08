package org.jglr.voxlin.entity

class BehaviorList(val entity: Entity, private val backingMap: MutableList<Behavior> = mutableListOf<Behavior>()) : MutableList<Behavior> by backingMap {

    override fun add(element: Behavior): Boolean {
        if(contains(element))
            throw IllegalArgumentException("Entity already has behavior $element")
        return backingMap.add(element)
    }

    inline fun <reified T : Behavior> has(): Boolean {
        return any { it is T }
    }

    inline fun <reified T : Behavior> get(): T {
        return filter { it is T }.first() as T
    }
}