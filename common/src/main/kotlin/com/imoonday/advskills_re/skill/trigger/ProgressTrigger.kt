package com.imoonday.advskills_re.skill.trigger

import net.minecraft.entity.player.*

interface ProgressTrigger : SkillTrigger {

    fun shouldDisplay(player: PlayerEntity): Boolean

    fun getProgress(player: PlayerEntity): Double

    fun shouldFlashIcon(player: PlayerEntity): Boolean = player.isUsing()

    fun canBeEmpty(player: PlayerEntity): Boolean = false
}