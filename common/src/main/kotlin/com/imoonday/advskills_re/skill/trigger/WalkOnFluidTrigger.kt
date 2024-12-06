package com.imoonday.advskills_re.skill.trigger

import net.minecraft.entity.player.*
import net.minecraft.fluid.*

interface WalkOnFluidTrigger : SkillTrigger {

    fun canWalkOnFluid(player: PlayerEntity, state: FluidState): Boolean = player.isUsing()
}