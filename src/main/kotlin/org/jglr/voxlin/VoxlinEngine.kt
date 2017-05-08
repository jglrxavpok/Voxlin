package org.jglr.voxlin

import org.lwjgl.glfw.GLFW

class VoxlinEngine(val game: Game, val windowHandle: Long) {

    private var running: Boolean = false

    internal fun mainLoop() {
        running = true

        // convert the time to seconds
        var nextTime = System.nanoTime().toDouble() / 1000000000.0
        val maxTimeDiff = 0.5
        var skippedFrames = 1
        val maxSkippedFrames = 5
        val rate = (1f / 60f).toDouble()
        var lastTime = nextTime
        while (running) {
            // convert the time to seconds
            val currTime = System.nanoTime().toDouble() / 1000000000.0
            val delta = currTime - nextTime
            if (delta > maxTimeDiff) nextTime = currTime
            if (delta >= 0f) {
                // assign the time for the next update
                nextTime += rate
                val deltaTime = (currTime - lastTime).toFloat()

                lastTime = currTime
                update(deltaTime)
                if (currTime < nextTime || skippedFrames > maxSkippedFrames) {
                    val interpolation = Math.max(1.0, delta * 1000000.0)
                    render(deltaTime)
                    skippedFrames = 1
                } else {
                    skippedFrames++
                }
            } else {
                // calculate the time to sleep
                val sleepTime = (100.0 * (nextTime - currTime)).toInt()
                // sanity check
                if (sleepTime > 0) {
                    // sleep until the next update
                    Thread.sleep(sleepTime.toLong())
                }
            }
            running = game.isRunning && !GLFW.glfwWindowShouldClose(windowHandle)
        }
    }

    private fun render(interpolation: Float) {
        game.render(interpolation)
        GLFW.glfwSwapBuffers(windowHandle)
    }

    private fun update(deltaTime: Float) {
        GLFW.glfwPollEvents()
        game.update(deltaTime)
    }
}