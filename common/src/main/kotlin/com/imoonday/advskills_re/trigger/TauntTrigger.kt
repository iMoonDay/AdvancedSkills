package com.imoonday.advskills_re.trigger

import net.minecraft.entity.player.*

interface TauntTrigger : SkillTrigger {

    fun isTaunting(player: PlayerEntity): Boolean = player.isUsing()
}
