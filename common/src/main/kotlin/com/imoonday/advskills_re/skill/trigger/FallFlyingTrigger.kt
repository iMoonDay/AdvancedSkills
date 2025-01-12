package com.imoonday.advskills_re.skill.trigger

import net.minecraft.entity.player.*

interface FallFlyingTrigger : SkillTrigger {

    fun canStartFallFlying(player: PlayerEntity): Boolean
}