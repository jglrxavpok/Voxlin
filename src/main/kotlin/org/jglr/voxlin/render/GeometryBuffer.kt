package org.jglr.voxlin.render

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glDrawBuffers
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil

enum class GeometryBufferTextureType {
    Position,
    Diffuse,
    Normal,
    Texcoords
}

class GeometryBuffer(val width: Int, val height: Int) {

    private val id: Int = glGenFramebuffers()

    private val m_textures: IntArray = IntArray(GeometryBufferTextureType.values().size)
    private val m_depthTexture: Int

    init {
        // Create the FBO
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, id)

        // Create the gbuffer textures
        glGenTextures(m_textures)
        m_depthTexture = glGenTextures()

        for (i in 0..(m_textures.size)-1) {
            glBindTexture(GL_TEXTURE_2D, m_textures[i])
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, width, height, 0, GL_RGB, GL_FLOAT, MemoryUtil.NULL)
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i, GL_TEXTURE_2D, m_textures[i], 0)
        }

        // depth
        glBindTexture(GL_TEXTURE_2D, m_depthTexture)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT,
                MemoryUtil.NULL)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, m_depthTexture, 0);

        val drawBuffers = intArrayOf(GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2, GL_COLOR_ATTACHMENT3)
        glDrawBuffers(drawBuffers)

        val status = glCheckFramebufferStatus(GL_FRAMEBUFFER);

        if (status != GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("FB error, status: 0x${Integer.toHexString(status)}") // TODO Better exception
        }

        // restore default FBO
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0)
    }

    fun bindWriting() {
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, id)
    }

    fun bindReading() {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, id)
    }
}