package com.imoonday.advskills_re.trigger

import net.minecraft.entity.player.PlayerEntity

interface TauntTrigger : SkillTrigger {

    fun isTaunting(player: PlayerEntity): Boolean = player.isUsing()
}
