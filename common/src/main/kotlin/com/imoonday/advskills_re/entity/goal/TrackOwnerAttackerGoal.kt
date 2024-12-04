package com.imoonday.advskills_re.entity.goal

import com.imoonday.advskills_re.entity.*
import net.minecraft.entity.*
import net.minecraft.entity.ai.*
import net.minecraft.entity.ai.goal.*
import net.minecraft.entity.mob.*
import java.util.*

class TrackOwnerAttackerGoal<T>(private val servant: T) :
    TrackTargetGoal(servant, false) where T : MobEntity, T : Servant {

    private var attacker: LivingEntity? = null
    private var lastAttackedTime = 0

    init {
        this.controls = EnumSet.of(Control.TARGET)
    }

    override fun canStart(): Boolean {
        val livingEntity = servant.owner as? LivingEntity
        if (livingEntity == null) {
            return false
        } else {
            this.attacker = livingEntity.attacker
            return livingEntity.lastAttackedTime != this.lastAttackedTime
                && this.canTrack(this.attacker, TargetPredicate.DEFAULT)
                && servant.canAttackWithOwner(this.attacker, livingEntity)
        }
    }

    override fun start() {
        mob.target = this.attacker
        val livingEntity = servant.owner as? LivingEntity
        if (livingEntity != null) {
            this.lastAttackedTime = livingEntity.lastAttackedTime
        }

        super.start()
    }
}