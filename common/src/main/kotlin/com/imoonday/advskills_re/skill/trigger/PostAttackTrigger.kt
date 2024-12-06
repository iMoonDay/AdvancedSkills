package com.imoonday.advskills_re.skill.trigger

import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

interface PostAttackTrigger : SkillTrigger {

    fun postAttack(source: DamageSource, player: ServerPlayerEntity, target: LivingEntity) = Unit

    fun postSweepAttack(player: PlayerEntity, target: LivingEntity) = Unit
}