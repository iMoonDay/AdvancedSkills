package com.imoonday.triggers

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity

interface AttackTrigger {

    fun onAttack(amount: Float, source: DamageSource, attacker: ServerPlayerEntity, entity: LivingEntity): Float
}