package org.jglr.voxlin.entity.behaviors

import org.jglr.voxlin.entity.Behavior
import org.jglr.voxlin.entity.Entity

class PlayerAttackCoordinator(owner: Entity): Behavior(owner) {

    private val melee by lazy { owner.behaviors.get<MeleeAttackBehavior>() }
    private val range by lazy { owner.behaviors.get<RangeAttackBehavior>() }

    override fun update(delta: Float) {

    }

    fun getCurrentAttack(): AttackBehavior? {
        if(melee.isAttacking())
            return melee
        if(range.isAttacking())
            return range
        return null
    }

    fun meleeAttack(): Boolean {
        val canAttack =  ! isAttacking()
        if(canAttack)
            melee.attack()
        return canAttack
    }

    fun isAttacking(): Boolean  = range.isAttacking() or melee.isAttacking()

    fun rangeAttack(): Boolean {
        val canAttack =  ! isAttacking()
        if(canAttack)
            range.attack()
        return canAttack
    }
}