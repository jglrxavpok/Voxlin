package org.jglr.voxlin.math

import org.joml.Vector2f
import org.joml.Vector2fc

class BoundingBox(val size: Vector2fc) {

    val position = Vector2f()

    fun intersects(other: BoundingBox): Boolean {
        if(position.x() >= other.position.x() + other.size.x())
            return false
        if(position.y() >= other.position.y() + other.size.y())
            return false
        if(position.x()+size.x() <= other.position.x())
            return false
        if(position.y()+size.y() <= other.position.y())
            return false
        return true
    }
}