package com.imoonday.trigger

import net.minecraft.entity.player.*

interface StopTrigger : SkillTrigger {

    fun postStop(player: PlayerEntity) = Unit
}