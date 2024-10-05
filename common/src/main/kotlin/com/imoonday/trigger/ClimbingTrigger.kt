package com.imoonday.trigger

import net.minecraft.entity.player.PlayerEntity

interface ClimbingTrigger : SkillTrigger {

    fun isClimbing(player: PlayerEntity): Boolean = player.isUsing()
}