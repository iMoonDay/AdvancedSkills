package com.imoonday.advskills_re.trigger

import net.minecraft.entity.player.*

interface BreatheInWaterTrigger : SkillTrigger {

    fun canBreatheInWater(player: PlayerEntity): Boolean = player.isUsing()
}