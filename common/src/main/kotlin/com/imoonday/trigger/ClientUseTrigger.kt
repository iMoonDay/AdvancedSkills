package com.imoonday.trigger

import net.minecraft.entity.player.PlayerEntity

interface ClientUseTrigger : SkillTrigger {

    fun onUse(player: PlayerEntity) = Unit

    fun onStop(player: PlayerEntity) = Unit
}