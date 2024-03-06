package com.imoonday.trigger

import net.minecraft.entity.player.PlayerEntity

interface StepHeightTrigger {

    fun getStepHeight(player: PlayerEntity): Float?
}