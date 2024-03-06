package com.imoonday.trigger

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.FluidState

interface WalkOnFluidTrigger {

    fun canWalkOnFluid(player: PlayerEntity, state: FluidState): Boolean
}