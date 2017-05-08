package org.jglr.voxlin.render

import org.jglr.voxlin.Transform
import org.joml.*
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import java.nio.FloatBuffer
import java.nio.IntBuffer

class RenderBatch(var texture: Texture, defaultVertexCount: Int = 300, private val defaultIndexCount: Int = 100, private val usage: Int = GL_DYNAMIC_DRAW) {

    private val bufferID: Int
    private val indexBufferID: Int
    private val vaoID: Int
    private var vertexBuffer: FloatBuffer
    private var indexBuffer: IntBuffer
    private val vertexOffset: Int = 0
    private var vertexCount: Int = 0
    private val indexOffset: Int = 0
    private val defaultVertexCount: Int
    private val transformMatrix: MatrixStackf
    private val transformVector: Vector4f
    private val combineStack = MatrixStackf(50)
    private val combinedTransforms = Matrix4f().identity()
    private var transformDepth = 0
    val vertexTransformMatrix = Matrix4f().identity()

    init {
        vertexCount = defaultVertexCount
        this.defaultVertexCount = vertexCount
        vaoID = glGenVertexArrays()
        bind()

        bufferID = glGenBuffers()

        glBindBuffer(GL_ARRAY_BUFFER, bufferID)
        glBufferData(GL_ARRAY_BUFFER, (defaultVertexCount * VERTEX_SIZE).toLong(), usage)

        indexBufferID = glGenBuffers()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferID)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, defaultIndexCount.toLong(), usage)

        glVertexAttribPointer(ShaderProgram.VERTEX_POS_ATTRIBUTE, 3, GL_FLOAT, false, VERTEX_SIZE * 4, 0)
        glEnableVertexAttribArray(ShaderProgram.VERTEX_POS_ATTRIBUTE)

        glVertexAttribPointer(ShaderProgram.TEX_COORDS_ATTRIBUTE, 2, GL_FLOAT, false, VERTEX_SIZE * 4, (3 * 4).toLong())
        glEnableVertexAttribArray(org.jglr.voxlin.render.ShaderProgram.TEX_COORDS_ATTRIBUTE)

        glVertexAttribPointer(ShaderProgram.NORMAL_COORDS_ATTRIBUTE, 3, GL_FLOAT, false, VERTEX_SIZE * 4, (4 * (2 + 3)).toLong())
        glEnableVertexAttribArray(org.jglr.voxlin.render.ShaderProgram.NORMAL_COORDS_ATTRIBUTE)

        glVertexAttribPointer(ShaderProgram.COLOR_ATTRIBUTE, 4, GL_FLOAT, false, VERTEX_SIZE * 4, (4 * (2 + 3 + 3)).toLong())
        glEnableVertexAttribArray(org.jglr.voxlin.render.ShaderProgram.COLOR_ATTRIBUTE)

        vertexBuffer = BufferUtils.createFloatBuffer(defaultVertexCount)
        indexBuffer = BufferUtils.createIntBuffer(defaultIndexCount)

        transformMatrix = MatrixStackf(50)
        transformMatrix.identity()
        transformVector = Vector4f()

        unbind()
    }

    fun endVertex(): RenderBatch {
        if (indexBuffer.remaining() < 1) {
            val newBuffer = BufferUtils.createIntBuffer(indexBuffer.capacity() + 1)
            indexBuffer.rewind()
            newBuffer.put(indexBuffer)
            indexBuffer = newBuffer
        }
        indexBuffer.put(vertexCount)
        vertexCount++
        return this
    }

    fun texCoords(v: Vector2fc): RenderBatch {
        return texCoords(v.x(), v.y())
    }

    fun texCoords(x: Float, y: Float): RenderBatch {
        vertexData(x, y)
        return this
    }

    fun vertexTransformation(matrix: Matrix4fc): RenderBatch {
        vertexTransformMatrix.set(matrix)
        return this
    }

    fun resetTransform(): RenderBatch {
        transformMatrix.clear()
        transformDepth = 0
        combineTransforms()
        return this
    }

    fun currentTransformIdentity(): RenderBatch {
        transformMatrix.identity()
        combineTransforms()
        return this
    }

    fun pushTransform(): RenderBatch {
        transformMatrix.pushMatrix()
        transformMatrix.identity()
        transformDepth++
        combineTransforms()
        return this
    }

    fun popTransform(): RenderBatch {
        transformMatrix.popMatrix()
        transformDepth--
        combineTransforms()
        return this
    }

    fun transform(transform: Transform): RenderBatch {
        return transform(transform.toMatrix())
    }

    fun transform(transform: Matrix4fc): RenderBatch {
        transformMatrix.mul(transform)
        combineTransforms()
        return this
    }

    fun pos(v: Vector2fc): RenderBatch {
        return pos(v.x(), v.y(), 0f)
    }

    fun pos(v: Vector3fc): RenderBatch {
        return pos(v.x(), v.y(), v.z())
    }

    fun pos(x: Float, y: Float, z: Float = 0f): RenderBatch {
        transformVector.set(x, y, z, 1f)
        vertexTransformMatrix.transform(transformVector)
        vertexData(transformVector.x, transformVector.y, transformVector.z)
        return this
    }

    fun normal(v: Vector2fc): RenderBatch {
        return normal(v.x(), v.y(), 0f)
    }

    fun normal(v: Vector3fc): RenderBatch {
        return normal(v.x(), v.y(), v.z())
    }

    fun normal(x: Float, y: Float, z: Float): RenderBatch {
        vertexData(x, y, z)
        return this
    }

    fun nonormal(): RenderBatch = normal(0f, 0f, 0f)

    fun color(rgbaColor: Vector4fc): RenderBatch {
        return color(rgbaColor.x(), rgbaColor.y(), rgbaColor.z(), rgbaColor.w())
    }

    fun color(rgbColor: Vector3fc): RenderBatch {
        return color(rgbColor.x(), rgbColor.y(), rgbColor.z(), 1f)
    }

    fun color(r: Float, g: Float, b: Float, a: Float = 1f): RenderBatch {
        vertexData(r, g, b, a)
        return this
    }

    private fun vertexData(vararg values: Float) {
        if (vertexBuffer.remaining() < values.size) {
            val newBuffer = BufferUtils.createFloatBuffer(vertexBuffer.capacity() + values.size)
            vertexBuffer.rewind()
            newBuffer.put(vertexBuffer)
            vertexBuffer = newBuffer
        }
        vertexBuffer.put(values)
    }

    fun reset() {
        vertexCount = 0
        vertexBuffer.clear()
        indexBuffer.clear()
    }

    fun bind() {
        glBindVertexArray(vaoID)
        glBindBuffer(GL_ARRAY_BUFFER, bufferID)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferID)
    }

    fun unbind() {
        glBindVertexArray(0)
    }

    fun upload() {
        vertexBuffer.flip()
        indexBuffer.flip()
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, usage)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, usage)
        vertexBuffer.flip()
        indexBuffer.flip()
    }

    private fun combineTransforms(): Matrix4f {
        combinedTransforms.identity()
        combineStack.clear()
        combineStack.set(transformMatrix)
        combinedTransforms.identity()
        val prevDepth = transformDepth
        while(transformDepth > 0) {
            val top = transformMatrix

            // 'manual' pop in order to avoid recursive calls from popTransform
            transformMatrix.popMatrix()
            transformDepth--

            top.mul(combinedTransforms, combinedTransforms) // top * combined; order is important
            combineStack.set(top)
            combineStack.pushMatrix()
        }

        // restores the matrices onto the stack
        while(transformDepth != prevDepth) {
            transformMatrix.set(combineStack.popMatrix())

            // 'manual' pop in order to avoid recursive calls from pushTransform
            transformMatrix.pushMatrix()
            transformDepth++
        }
        transformMatrix.set(combineStack)

        return combinedTransforms
    }

    fun performRenderCall(shader: ShaderProgram) {
        shader.setUniform4x4f(shader.getLocation("u_modelview"), combinedTransforms)

        texture.bind()
        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0)
    }

    companion object {

        val VERTEX_SIZE = 3 + 2 + 3 + 4 // pos+tex+normal+color
    }
}
