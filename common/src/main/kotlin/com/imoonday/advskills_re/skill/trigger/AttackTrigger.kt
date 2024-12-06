package com.imoonday.advskills_re.skill.trigger

import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

interface AttackTrigger : SkillTrigger {

    fun onAttack(amount: Float, source: DamageSource, player: ServerPlayerEntity, target: LivingEntity): Float = amount
}