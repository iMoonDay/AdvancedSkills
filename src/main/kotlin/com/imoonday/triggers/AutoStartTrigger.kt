package com.imoonday.triggers

import net.minecraft.server.network.ServerPlayerEntity

interface AutoStartTrigger {

    fun onStart(player: ServerPlayerEntity){}
}