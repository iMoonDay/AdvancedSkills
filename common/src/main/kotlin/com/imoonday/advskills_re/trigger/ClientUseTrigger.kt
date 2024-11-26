package com.imoonday.advskills_re.trigger

import net.minecraft.entity.player.*

interface ClientUseTrigger : SkillTrigger {

    fun onUse(player: PlayerEntity) = Unit

    fun onStop(player: PlayerEntity) = Unit
}