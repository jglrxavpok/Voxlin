package org.jglr.voxlin

import org.jglr.voxlin.Game
import org.jglr.voxlin.VoxlinEngine
import org.lwjgl.glfw.GLFWCursorPosCallback
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.glfw.GLFWWindowSizeCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GLUtil
import org.lwjgl.system.MemoryUtil
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import javax.swing.*

object VoxlinMain {

    private var windowHandle: Long = 0
    private var game: Game? = null

    const val WIDTH = 1024 // TODO: Make it modifiable
    const val HEIGHT = 576

    private fun start() {
        if (!initGL()) {
            errorAtInit("Failed to create OpenGL context")
            return
        }
        initGame()
        if (game == null) {
            errorAtInit("Failed to initialize game")
            return
        }
        if (!initControls()) {
            errorAtInit("Failed to initialize controls")
            return
        }
        VoxlinEngine(game!!, windowHandle).mainLoop()
    }

    private fun initControls(): Boolean {
        glfwSetCursorPosCallback(windowHandle, object : GLFWCursorPosCallback() {

            private var lastX: Double = 0.toDouble()
            private var lastY: Double = 0.toDouble()

            override fun invoke(window: Long, xpos: Double, ypos: Double) {
                val dx = xpos - lastX
                val dy = ypos - lastY
                game!!.onMouseMoved(xpos, ypos, dx, dy)
                lastX = xpos
                lastY = ypos
            }
        })

        glfwSetKeyCallback(windowHandle, object : GLFWKeyCallback() {
            override fun invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
                if (action == GLFW_PRESS) {
                    game!!.keyPress(key, mods)
                } else if (action == GLFW_RELEASE) {
                    game!!.keyRelease(key, mods)
                }
            }
        })

        glfwSetWindowSizeCallback(windowHandle, object : GLFWWindowSizeCallback() {
            override fun invoke(window: Long, width: Int, height: Int) {
                game!!.resize(width, height)
            }
        })
        return true
    }

    private fun initGame(): Game? {
        Game.instance = Game()
        game = Game.instance
        return Game.instance
    }

    private fun errorAtInit(message: String) {
        JOptionPane.showMessageDialog(null, message, "Resources War - Error", JOptionPane.ERROR_MESSAGE)
    }

    private fun initGL(): Boolean {
        val glfwInit = glfwInit()
        if (!glfwInit) {
            return false
        }

        GLFWErrorCallback.createPrint(System.err).set()

        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        windowHandle = glfwCreateWindow(WIDTH, HEIGHT, "Voxlin", MemoryUtil.NULL, MemoryUtil.NULL)
        if (windowHandle == MemoryUtil.NULL)
            return false

        glfwMakeContextCurrent(windowHandle)
        glfwShowWindow(windowHandle)

        GL.createCapabilities()

        glEnable (GL_BLEND); glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        //GLUtil.setupDebugMessageCallback(System.err) // TODO
        return true
    }

    @JvmStatic fun main(args: Array<String>) {
        start()
    }
}