package com.imoonday.trigger

import net.minecraft.server.network.ServerPlayerEntity

interface AutoStartTrigger {

    fun onStart(player: ServerPlayerEntity){}
}