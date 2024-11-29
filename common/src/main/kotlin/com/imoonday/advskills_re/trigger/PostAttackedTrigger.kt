package com.imoonday.advskills_re.trigger

import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

interface PostAttackedTrigger : SkillTrigger {

    fun postAttacked(source: DamageSource, player: ServerPlayerEntity, attacker: LivingEntity?) = Unit
}