package com.imoonday.trigger

import net.minecraft.entity.player.*

interface InvisibilityTrigger : SkillTrigger {

    fun isInvisible(player: PlayerEntity): Boolean = player.isUsing()
    fun isInvisibleTo(player: PlayerEntity, otherPlayer: PlayerEntity): Boolean = true
}