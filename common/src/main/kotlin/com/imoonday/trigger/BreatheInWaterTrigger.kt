package com.imoonday.trigger

import net.minecraft.entity.player.PlayerEntity

interface BreatheInWaterTrigger : SkillTrigger {

    fun canBreatheInWater(player: PlayerEntity): Boolean = player.isUsing()
}