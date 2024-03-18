package com.imoonday.trigger

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity

interface PostDamagedTrigger : SkillTrigger {

    fun postDamaged(amount: Float, source: DamageSource, player: ServerPlayerEntity, attacker: LivingEntity?) = Unit
}