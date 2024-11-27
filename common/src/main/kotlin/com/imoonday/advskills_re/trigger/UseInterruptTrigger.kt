package com.imoonday.advskills_re.trigger

import net.minecraft.entity.player.*

interface UseInterruptTrigger : SkillTrigger {

    fun shouldInterrupt(player: PlayerEntity): Boolean = player.isUsing()

    fun interrupt(player: PlayerEntity)
}