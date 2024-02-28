package com.imoonday.triggers

import net.minecraft.server.network.ServerPlayerEntity

interface TickTrigger {

    fun tick(player: ServerPlayerEntity, usedTime: Int)
}