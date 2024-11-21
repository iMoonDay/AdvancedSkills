package com.imoonday.trigger

import net.minecraft.entity.player.*
import net.minecraft.fluid.*

interface WalkOnFluidTrigger : SkillTrigger {

    fun canWalkOnFluid(player: PlayerEntity, state: FluidState): Boolean = player.isUsing()
}