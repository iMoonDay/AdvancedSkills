package com.imoonday.trigger

import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

interface DeathTrigger : SkillTrigger {

    fun allowDeath(player: ServerPlayerEntity, source: DamageSource, amount: Float): Boolean
}