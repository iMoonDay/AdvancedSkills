package com.imoonday.advskills_re.skill.trigger.client.render

import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*

interface CrosshairTrigger : SkillTrigger {

    fun shouldRenderCrosshair(player: PlayerEntity): Boolean = getCrosshair(player) != Crosshairs.NONE

    fun getCrosshair(player: PlayerEntity): Crosshair = Crosshairs.NONE

    fun getPriority(player: PlayerEntity): Int = getCrosshair(player).priority
}