package com.imoonday.triggers

import net.minecraft.entity.player.PlayerEntity

interface ClimbingTrigger {

    fun isClimbing(player: PlayerEntity): Boolean
}