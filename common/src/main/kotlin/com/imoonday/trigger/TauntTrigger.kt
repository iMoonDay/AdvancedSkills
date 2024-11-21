package com.imoonday.trigger

import net.minecraft.entity.player.PlayerEntity

interface TauntTrigger : SkillTrigger {

    fun isTaunting(player: PlayerEntity): Boolean = player.isUsing()
}
