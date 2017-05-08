package org.jglr.voxlin.render

import org.jglr.voxlin.utils.ResourceLocation
import org.joml.Matrix3f
import org.joml.Matrix3fc
import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11

import java.nio.FloatBuffer

import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL32.*
import org.lwjgl.opengl.GL43.GL_COMPUTE_SHADER

class ShaderProgram(vararg shaderSources: String) {
    private val programID: Int
    private val matArray3x3 = BufferUtils.createFloatBuffer(3 * 3)
    private val matArray4x4 = BufferUtils.createFloatBuffer(4 * 4)

    constructor(vararg locations: ResourceLocation) : this(*locations.map { it.readText() }.toTypedArray())

    init {
        programID = glCreateProgram()
        for (source in shaderSources) {
            val type = readShaderType(source)
            val shaderID = glCreateShader(type)
            glShaderSource(shaderID, source)
            glCompileShader(shaderID)
            if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == 0) {
                val log = glGetShaderInfoLog(shaderID)
                System.err.println(log)
            }
            glAttachShader(programID, shaderID)
        }

        glLinkProgram(programID)
        if (glGetProgrami(programID, GL_LINK_STATUS) == 0) {
            val log = glGetProgramInfoLog(programID)
            System.err.println(log)
        }
    }

    fun bind() {
        glUseProgram(programID)
    }

    fun getLocation(name: String): Int {
        return glGetUniformLocation(programID, name)
    }

    fun setUniformi(location: Int, value: Int) {
        glUniform1i(location, value)
    }

    fun setUniformf(location: Int, value: Float) {
        glUniform1f(location, value)
    }

    fun setUniform2f(location: Int, v0: Float, v1: Float) {
        glUniform2f(location, v0, v1)
    }

    fun setUniform3f(location: Int, v0: Float, v1: Float, v2: Float) {
        glUniform3f(location, v0, v1, v2)
    }

    fun setUniform4f(location: Int, v0: Float, v1: Float, v2: Float, v3: Float) {
        glUniform4f(location, v0, v1, v2, v3)
    }

    fun setUniform3x3f(location: Int, mat: Matrix3fc) {
        matArray3x3.clear()
        mat.get(matArray3x3)
        setUniform3x3f(location, matArray3x3)
    }

    fun setUniform4x4f(location: Int, mat: Matrix4fc) {
        matArray4x4.clear()
        mat.get(matArray4x4)
        setUniform4x4f(location, matArray4x4)
    }

    fun setUniform3x3f(location: Int, buffer: FloatBuffer) {
        glUniformMatrix3fv(location, false, buffer)
    }

    fun setUniform4x4f(location: Int, buffer: FloatBuffer) {
        glUniformMatrix4fv(location, false, buffer)
    }

    private fun readShaderType(source: String): Int {
        if (source.startsWith("//!")) {
            var firstLine = source.substring(0, source.indexOf("\n"))
            firstLine = firstLine.replace("//!", "").trim { it <= ' ' }
            when (firstLine.toLowerCase()) {
                "vertex" -> return GL_VERTEX_SHADER

                "fragment" -> return GL_FRAGMENT_SHADER

                "geometry" -> return GL_GEOMETRY_SHADER
            }
        }
        return GL_COMPUTE_SHADER
    }

    companion object {

        val VERTEX_POS_ATTRIBUTE = 0
        val TEX_COORDS_ATTRIBUTE = 1
        val NORMAL_COORDS_ATTRIBUTE = 2
        val COLOR_ATTRIBUTE = 3
    }
}
