package com.imoonday.trigger

import net.minecraft.entity.player.PlayerEntity

interface FlipUpsideDownTrigger : SkillTrigger {

    fun shouldFlipUpsideDown(player: PlayerEntity): Boolean = false
}