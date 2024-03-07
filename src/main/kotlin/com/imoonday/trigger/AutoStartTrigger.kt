package com.imoonday.trigger

import net.minecraft.server.network.ServerPlayerEntity

interface AutoStartTrigger : SkillTrigger {

    fun onStart(player: ServerPlayerEntity) = Unit
}