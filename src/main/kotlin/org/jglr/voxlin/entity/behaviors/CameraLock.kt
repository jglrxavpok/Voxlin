package org.jglr.voxlin.entity.behaviors

import org.jglr.voxlin.Game
import org.jglr.voxlin.Transform
import org.jglr.voxlin.entity.Behavior
import org.jglr.voxlin.entity.Entity
import org.jglr.voxlin.level.Level
import org.jglr.voxlin.render.Camera
import org.joml.Vector2f
import org.joml.Vector3f

class CameraLock(owner: Entity, val camera: Camera) : Behavior(owner) {

    override fun update(delta: Float) {
        val transform = camera.transform
        val screenW = Game.screenWidth
        val screenH = Game.screenHeight
        val x = ((position.x()-boundingBox.size.x()/2f)*Level.TILES_TO_PIXELS.toFloat() - screenW/2f).coerceIn(0f, owner.level.width*Level.TILES_TO_PIXELS-screenW)
        val y = ((position.y()-boundingBox.size.y()/2f)*Level.TILES_TO_PIXELS.toFloat() - screenH/2f).coerceIn(0f, owner.level.height*Level.TILES_TO_PIXELS-screenH)
        transform.translation(-x, -y, 0f)
    }
}