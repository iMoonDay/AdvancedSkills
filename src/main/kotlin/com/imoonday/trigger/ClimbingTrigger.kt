package com.imoonday.trigger

import net.minecraft.entity.player.PlayerEntity

interface ClimbingTrigger {

    fun isClimbing(player: PlayerEntity): Boolean
}