package com.imoonday.trigger

import net.minecraft.entity.player.PlayerEntity

interface StepHeightTrigger : SkillTrigger {

    fun getStepHeight(player: PlayerEntity): Float?
}