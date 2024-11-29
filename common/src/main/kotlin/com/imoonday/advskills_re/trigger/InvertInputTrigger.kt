package com.imoonday.advskills_re.trigger

import net.minecraft.entity.player.*

interface InvertInputTrigger : SkillTrigger {

    fun shouldInvertInput(player: PlayerEntity): Boolean = false
    fun shouldInvertHorizontalInput(player: PlayerEntity): Boolean = shouldInvertInput(player)
    fun shouldInvertVerticalInput(player: PlayerEntity): Boolean = shouldInvertInput(player)
    fun shouldInvertJump(player: PlayerEntity): Boolean = shouldInvertInput(player)
    fun shouldInvertSneak(player: PlayerEntity): Boolean = shouldInvertInput(player)
}