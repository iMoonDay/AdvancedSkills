package com.imoonday.trigger

import net.minecraft.server.network.*

interface RespawnTrigger : SkillTrigger {

    fun afterRespawn(player: ServerPlayerEntity) = Unit
}