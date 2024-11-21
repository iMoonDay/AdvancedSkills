package com.imoonday.trigger

import net.minecraft.server.network.*

interface AutoStartTrigger : SkillTrigger {

    fun onStart(player: ServerPlayerEntity) = Unit
}