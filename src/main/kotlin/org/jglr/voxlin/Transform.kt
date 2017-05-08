package org.jglr.voxlin

import org.joml.*

class Transform {

    private val transformStack = MatrixStackf(32)

    init {
        transformStack.identity()
    }

    fun toMatrix(): Matrix4f = Matrix4f(transformStack)

    fun statePop(): Transform {
        transformStack.popMatrix()
        return this
    }

    fun statePush(): Transform {
        transformStack.pushMatrix()
        return this
    }

    fun stateDrop(): Transform {
        val saved = toMatrix()
        transformStack.popMatrix()
        transformStack.set(saved)
        return this
    }

    fun scale(vect: Vector3fc) = scale(vect.x(), vect.y(), vect.z())

    fun scale(xyz: Float): Transform {
        transformStack.scale(xyz)
        return this
    }

    fun scale(x: Float, y: Float, z: Float): Transform {
        transformStack.scale(x, y, z)
        return this
    }

    fun translate(vect: Vector3fc) = translate(vect.x(), vect.y(), vect.z())

    fun translate(x: Float, y: Float, z: Float): Transform {
        transformStack.translate(x, y, z)
        return this
    }

    fun rotate(angle: Float, x: Float, y: Float, z: Float): Transform {
        transformStack.rotate(angle, x, y, z)
        return this
    }

    fun translation(x: Float, y: Float, z: Float): Transform {
        transformStack.translation(x, y, z)
        return this
    }

    fun rotate(angle: AxisAngle4f) = rotate(angle.angle, angle.x, angle.y, angle.z)

    fun identity(): Transform {
        transformStack.identity()
        return this
    }
}