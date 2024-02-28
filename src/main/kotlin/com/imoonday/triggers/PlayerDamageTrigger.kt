package com.imoonday.triggers

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity

interface PlayerDamageTrigger {

    fun onDamaged(amount: Float, source: DamageSource, player: ServerPlayerEntity, attacker: LivingEntity?): Float
}