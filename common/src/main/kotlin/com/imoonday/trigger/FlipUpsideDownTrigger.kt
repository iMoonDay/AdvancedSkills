package com.imoonday.trigger

import net.minecraft.entity.player.*

interface FlipUpsideDownTrigger : SkillTrigger {

    fun shouldFlipUpsideDown(player: PlayerEntity): Boolean = false
}