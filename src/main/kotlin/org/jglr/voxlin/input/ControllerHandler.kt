package org.jglr.voxlin.input

import org.lwjgl.glfw.GLFW.*
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

object ControllerHandler {

    const val XBoxLeftX = 0
    const val XBoxLeftY = 1
    const val XBoxRightX = 2
    const val XBoxRightY = 3
    const val XBoxLeftTrigger = 4
    const val XBoxRightTrigger = 5
    private val joysticks = Array(10, ::Joystick)

    fun prepare() {
        glfwSetJoystickCallback { id, event ->
            if(event == GLFW_CONNECTED) {
                println("Joystick $id connected, name is ${glfwGetJoystickName(id)}")
                joysticks[id].connected = true
            } else if(event == GLFW_DISCONNECTED) {
                println("Joystick $id disconnected")
                joysticks[id].connected = false
            }
        }
    }

    fun update() {
        joysticks.filter { hasJoystick(it.id) }
                .forEach {
                    it.connected = true
                    it.buttons = glfwGetJoystickButtons(it.id)
                    it.axes = glfwGetJoystickAxes(it.id)
        }
    }

    operator fun get(index: Int): Joystick {
        return joysticks[index]
    }

    fun hasJoystick(index: Int): Boolean = glfwJoystickPresent(index)
}