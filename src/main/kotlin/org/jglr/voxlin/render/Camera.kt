package org.jglr.voxlin.render

import org.jglr.voxlin.Transform
import org.jglr.voxlin.Transformable
import org.joml.Matrix4f

class Camera(var projection: Matrix4f, val shader: ShaderProgram) : Transformable {

    private val viewMatrix = Matrix4f()
    private val identity = Matrix4f().identity().toImmutable()

    val transform = Transform()

    override fun transform(): Transform = transform

    fun getViewMatrix(): Matrix4f = viewMatrix

    fun update() {
        projection.mul(transform.toMatrix(), viewMatrix)
        shader.bind()
        shader.setUniform4x4f(shader.getLocation("u_viewMatrix"), viewMatrix)
        shader.setUniform4x4f(shader.getLocation("u_modelview"), identity)
    }
}