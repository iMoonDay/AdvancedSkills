package com.imoonday.advskills_re.trigger

import net.minecraft.server.network.*

interface AutoStartTrigger : SkillTrigger {

    fun onStart(player: ServerPlayerEntity) = Unit
}