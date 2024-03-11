package com.imoonday.trigger

import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity

interface DamageTrigger : SkillTrigger {

    fun onDamaged(amount: Float, source: DamageSource, player: ServerPlayerEntity, attacker: LivingEntity?): Float =
        amount

    fun ignoreDamage(amount: Float, source: DamageSource, player: ServerPlayerEntity, attacker: Entity?): Boolean =
        false
}