package com.imoonday.trigger

import net.minecraft.entity.player.*

interface StepHeightTrigger : SkillTrigger {

    fun getStepHeight(player: PlayerEntity): Float?
}