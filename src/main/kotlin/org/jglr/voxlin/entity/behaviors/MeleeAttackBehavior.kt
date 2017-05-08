
package org.jglr.voxlin.entity.behaviors

import org.jglr.voxlin.entity.Entity
import org.jglr.voxlin.math.BoundingBox
import org.joml.Vector2f

class MeleeAttackBehavior(owner: Entity): AttackBehavior(owner) {

    override val hitbox: BoundingBox = BoundingBox(Vector2f(2f))

    var attackTimer = 0
        private set

    override fun update(delta: Float) {
        if(attackTimer > 0) {
            hitbox.position.set(position)
            val hitboxMargin = -0.3f
            val direction = Math.signum(velocity.x)
            if(direction >= 0f) {
                hitbox.position.add(owner.boundingBox.size.x()+hitboxMargin, 0f)
            } else if(direction <= 0f) {
                hitbox.position.add(-hitbox.size.x()-hitboxMargin, 0f)
            }

            owner.level.getEntities().filterNot { it in alreadyTouched || it == owner }
                    .filter { it.boundingBox.intersects(hitbox) }
                    .forEach {
                        it.attack(owner, 2)
                        alreadyTouched += it
                    }
        }

        if(attackTimer == 1) {
            alreadyTouched.clear()
        }

        if(attackTimer > 0) {
            attackTimer--
        }
    }

    fun attack() {
        attackTimer = 10
    }

    override fun isAttacking(): Boolean = attackTimer > 0
}