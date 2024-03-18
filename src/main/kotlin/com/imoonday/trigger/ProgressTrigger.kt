package com.imoonday.trigger

import net.minecraft.entity.player.PlayerEntity

interface ProgressTrigger : SkillTrigger {

    fun shouldDisplay(player: PlayerEntity): Boolean = false
    fun getProgress(player: PlayerEntity): Double
}