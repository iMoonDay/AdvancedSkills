package com.imoonday.advskills_re.entity.goal

import com.imoonday.advskills_re.entity.*
import net.minecraft.entity.*
import net.minecraft.entity.ai.*
import net.minecraft.entity.ai.goal.*
import net.minecraft.entity.mob.*
import java.util.*

class AttackWithOwnerGoal<T>(private val servant: T) :
    TrackTargetGoal(servant, false) where T : MobEntity, T : Servant {

    private var attacking: LivingEntity? = null
    private var lastAttackTime = 0

    init {
        this.controls = EnumSet.of(Control.TARGET)
    }

    override fun canStart(): Boolean {
        val livingEntity = servant.owner as LivingEntity?
        if (livingEntity == null) {
            return false
        } else {
            this.attacking = livingEntity.attacking
            return livingEntity.lastAttackTime != this.lastAttackTime
                && this.canTrack(this.attacking, TargetPredicate.DEFAULT)
                && servant.canAttackWithOwner(this.attacking, livingEntity)
        }
    }

    override fun start() {
        mob.target = this.attacking
        val livingEntity = servant.owner as LivingEntity?
        if (livingEntity != null) {
            this.lastAttackTime = livingEntity.lastAttackTime
        }

        super.start()
    }
}