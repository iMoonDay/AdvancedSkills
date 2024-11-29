package com.imoonday.advskills_re.trigger

import net.minecraft.entity.player.*

interface DisguiseTrigger : SkillTrigger {

    fun isDisguising(player: PlayerEntity): Boolean = player.isUsing()
}