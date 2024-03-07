package com.imoonday.trigger

import net.minecraft.server.network.ServerPlayerEntity

interface TickTrigger : SkillTrigger {

    fun tick(player: ServerPlayerEntity, usedTime: Int) = Unit
}