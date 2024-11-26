package com.imoonday.advskills_re.trigger

import net.minecraft.server.network.*

interface LandingTrigger : SkillTrigger {

    fun onLanding(player: ServerPlayerEntity, height: Float) = Unit
}