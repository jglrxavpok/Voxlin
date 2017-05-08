package org.jglr.voxlin.input

import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.GLFW_PRESS
import java.nio.ByteBuffer
import java.nio.FloatBuffer

class Joystick(val id: Int) {

    var axes: FloatBuffer = FloatBuffer.allocate(0)
        internal set
    var buttons: ByteBuffer = ByteBuffer.allocate(0)
        internal set
    var connected: Boolean = false
        internal set

    fun button(index: Int): Boolean = buttons[index].toInt() == GLFW_PRESS

    fun axis(index: Int): Float = axes[index]
}