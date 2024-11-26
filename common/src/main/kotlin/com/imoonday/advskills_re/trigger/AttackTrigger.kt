package com.imoonday.advskills_re.trigger

import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

interface AttackTrigger : SkillTrigger {

    fun onAttack(amount: Float, source: DamageSource, player: ServerPlayerEntity, target: LivingEntity): Float = amount

    fun postSweepAttack(player: PlayerEntity, target: LivingEntity) = Unit
}