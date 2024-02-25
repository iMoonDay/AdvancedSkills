package com.imoonday.trigger

import net.minecraft.server.network.ServerPlayerEntity

interface RespawnTrigger {

    fun afterRespawn(oldPlayer: ServerPlayerEntity, newPlayer: ServerPlayerEntity, alive: Boolean)
}