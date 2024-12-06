package com.imoonday.advskills_re.skill.trigger

import net.minecraft.entity.player.*

interface FlipUpsideDownTrigger : SkillTrigger {

    fun shouldFlipUpsideDown(player: PlayerEntity): Boolean = false
}