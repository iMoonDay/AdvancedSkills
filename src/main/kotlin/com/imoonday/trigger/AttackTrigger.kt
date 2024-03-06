package com.imoonday.trigger

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity

interface AttackTrigger {

    fun onAttack(amount: Float, source: DamageSource, player: ServerPlayerEntity, target: LivingEntity): Float
}