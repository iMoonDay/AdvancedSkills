package com.imoonday.advskills_re.trigger

import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

interface DamageTrigger : SkillTrigger {

    fun onDamaged(amount: Float, source: DamageSource, player: ServerPlayerEntity, attacker: LivingEntity?): Float =
        amount

    fun ignoreDamage(amount: Float, source: DamageSource, player: ServerPlayerEntity, attacker: Entity?): Boolean =
        false
}