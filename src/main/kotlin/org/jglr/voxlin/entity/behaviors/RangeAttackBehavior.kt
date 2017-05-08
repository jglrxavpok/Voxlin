
package org.jglr.voxlin.entity.behaviors

import org.jglr.voxlin.entity.Behavior
import org.jglr.voxlin.entity.Entity
import org.jglr.voxlin.math.BoundingBox
import org.jglr.voxlin.math.easeInCircular
import org.jglr.voxlin.math.easeInLinear
import org.jglr.voxlin.math.easeOutCircular
import org.joml.Vector2f

// TODO: Rename or create sub class Boomerang ?
class RangeAttackBehavior(owner: Entity): AttackBehavior(owner) {

    override val hitbox: BoundingBox = BoundingBox(Vector2f(2f))

    private var launched = false
    private var speed = 0f
    private var secondPhaseTime = 0f
    private val secondPhaseStartPos = Vector2f()

    override fun update(delta: Float) {
        if( ! launched)
            return

        owner.level.getEntities().filterNot { it in alreadyTouched || it == owner }
                .filter { it.boundingBox.intersects(hitbox) }
                .forEach {
                    it.attack(owner, 1) // TODO: Change value?
                    alreadyTouched += it
                }

        // arc
        if(Math.abs(speed) >= 1f) { // first phase: thrown
            speed *= 0.8f
            hitbox.position.x += speed * delta
            secondPhaseStartPos.set(hitbox.position)
            secondPhaseTime = 0f

        } else { // second phase: come back
            val newPosX = easeOutCircular(secondPhaseTime.toDouble(), secondPhaseStartPos.x.toDouble(), position.x.toDouble(), 0.5)
            val newPosY = easeInCircular(secondPhaseTime.toDouble(), secondPhaseStartPos.y.toDouble(), position.y.toDouble(), 0.5)
            hitbox.position.set(newPosX.toFloat(), newPosY.toFloat())
            secondPhaseTime += delta
            if(secondPhaseTime >= 1f)
                secondPhaseTime = 1f

            if(hitbox.intersects(owner.boundingBox)) {
                launched = false
                alreadyTouched.clear()
            }
        }
    }

    fun attack() {
        launched = true
        var speedSign = Math.signum(velocity.x)
        if(speedSign.toInt() == 0) {
            speedSign = 1f
        }
        speed = 8f*(speedSign * 20f + velocity.x)
        hitbox.position.set(position)
    }

    override fun isAttacking(): Boolean {
        return launched
    }
}
