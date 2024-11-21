package com.imoonday.trigger

import net.minecraft.server.network.*

interface RespawnTrigger : SkillTrigger {

    fun afterRespawn(oldPlayer: ServerPlayerEntity, newPlayer: ServerPlayerEntity, alive: Boolean) = Unit
}