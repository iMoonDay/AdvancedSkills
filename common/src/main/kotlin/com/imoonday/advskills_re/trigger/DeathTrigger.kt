package com.imoonday.advskills_re.trigger

import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

interface DeathTrigger : SkillTrigger {

    fun allowDeath(player: ServerPlayerEntity, source: DamageSource, amount: Float): Boolean = true

    fun onDeath(player: ServerPlayerEntity, source: DamageSource) = Unit
}