package com.imoonday.trigger

import net.minecraft.server.network.ServerPlayerEntity

interface LandingTrigger {
    fun onLanding(player: ServerPlayerEntity, height: Float)
}