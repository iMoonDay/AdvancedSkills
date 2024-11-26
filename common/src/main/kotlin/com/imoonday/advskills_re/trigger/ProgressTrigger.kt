package com.imoonday.advskills_re.trigger

import net.minecraft.entity.player.*

interface ProgressTrigger : SkillTrigger {

    fun shouldDisplay(player: PlayerEntity): Boolean = false
    fun getProgress(player: PlayerEntity): Double
    fun shouldFlashIcon(): Boolean = true
}