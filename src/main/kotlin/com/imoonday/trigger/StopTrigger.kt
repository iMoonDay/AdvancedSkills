package com.imoonday.trigger

import net.minecraft.entity.player.PlayerEntity

interface StopTrigger : SkillTrigger {

    fun postStop(player: PlayerEntity) = Unit
}