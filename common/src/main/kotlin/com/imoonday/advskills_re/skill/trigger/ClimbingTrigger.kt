package com.imoonday.advskills_re.skill.trigger

import net.minecraft.entity.player.*

interface ClimbingTrigger : SkillTrigger {

    fun isClimbing(player: PlayerEntity): Boolean = player.isUsing()
}