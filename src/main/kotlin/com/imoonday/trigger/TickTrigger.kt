package com.imoonday.trigger

import net.minecraft.server.network.ServerPlayerEntity

interface TickTrigger {

    fun tick(player: ServerPlayerEntity, usedTime: Int)
}