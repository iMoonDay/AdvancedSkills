package com.imoonday.trigger

import net.minecraft.server.network.ServerPlayerEntity

interface LandingTrigger : SkillTrigger {

    fun onLanding(player: ServerPlayerEntity, height: Float) = Unit
}