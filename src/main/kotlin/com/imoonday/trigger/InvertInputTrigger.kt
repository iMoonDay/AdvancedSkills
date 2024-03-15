package com.imoonday.trigger

import net.minecraft.entity.player.PlayerEntity

interface InvertInputTrigger : SkillTrigger {

    fun shouldInvertInput(): Boolean = false
    fun shouldInvertHorizontalInput(): Boolean = shouldInvertInput()
    fun shouldInvertVerticalInput(): Boolean = shouldInvertInput()
    fun shouldInvertJump(player: PlayerEntity): Boolean = shouldInvertInput()
    fun shouldInvertSneak(player: PlayerEntity): Boolean = shouldInvertInput()
}