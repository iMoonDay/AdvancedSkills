package com.imoonday.trigger

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity

interface DamageTrigger {

    fun onDamaged(amount: Float, source: DamageSource, entity: LivingEntity, attacker: ServerPlayerEntity): Float
}