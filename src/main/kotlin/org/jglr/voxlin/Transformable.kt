package org.jglr.voxlin

import org.joml.AxisAngle4f
import org.joml.Vector3fc

interface Transformable {

    fun transform(): Transform

    fun transformStatePop(): Transformable {
        transform().statePop()
        return this
    }

    fun transformStatePush(): Transformable {
        transform().statePush()
        return this
    }

    fun scale(vect: Vector3fc): Transformable {
        transform().scale(vect.x(), vect.y(), vect.z())
        return this
    }

    fun scale(xyz: Float): Transformable {
        transform().scale(xyz)
        return this
    }

    fun scale(x: Float, y: Float, z: Float): Transformable {
        transform().scale(x, y, z)
        return this
    }

    fun translate(vect: Vector3fc): Transformable {
        translate(vect.x(), vect.y(), vect.z())
        return this
    }

    fun translate(x: Float, y: Float, z: Float): Transformable {
        transform().translate(x, y, z)
        return this
    }

    fun rotate(angle: Float, x: Float, y: Float, z: Float): Transformable {
        transform().rotate(angle, x, y, z)
        return this
    }

    fun rotate(angle: AxisAngle4f): Transformable {
        rotate(angle.angle, angle.x, angle.y, angle.z)
        return this
    }

}