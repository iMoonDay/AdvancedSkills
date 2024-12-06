package com.imoonday.advskills_re.skill.trigger.client

import com.imoonday.advskills_re.skill.trigger.*
import net.minecraft.entity.player.*

interface InvertMouseTrigger : SkillTrigger {

    fun shouldInvertMouse(player: PlayerEntity): Boolean = false

    fun shouldInvertMouseX(player: PlayerEntity): Boolean = shouldInvertMouse(player)

    fun shouldInvertMouseY(player: PlayerEntity): Boolean = shouldInvertMouse(player)
}