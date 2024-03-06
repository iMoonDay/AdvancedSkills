package com.imoonday.trigger

import net.minecraft.server.network.ServerPlayerEntity

interface FallTrigger {

    fun onFall(amount: Int, player: ServerPlayerEntity, fallDistance: Float, damageMultiplier: Float): Int
}