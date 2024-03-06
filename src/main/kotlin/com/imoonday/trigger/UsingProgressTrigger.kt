package com.imoonday.trigger

import net.minecraft.entity.player.PlayerEntity

interface UsingProgressTrigger {
    fun shouldDisplay(player: PlayerEntity): Boolean = true
    fun getProgress(player: PlayerEntity): Double
}