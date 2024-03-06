package com.imoonday.trigger

import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity

interface DeathTrigger {

    fun allowDeath(player: ServerPlayerEntity, source: DamageSource, amount: Float): Boolean
}