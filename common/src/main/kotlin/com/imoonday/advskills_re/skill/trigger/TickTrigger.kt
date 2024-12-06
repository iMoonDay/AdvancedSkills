package com.imoonday.advskills_re.skill.trigger

import net.minecraft.entity.player.*
import net.minecraft.server.network.*

interface TickTrigger : SkillTrigger {

    fun serverTick(player: ServerPlayerEntity, usedTime: Int) = tick(player, usedTime)

    fun clientTick(player: PlayerEntity, usedTime: Int) = tick(player, usedTime)

    fun tick(player: PlayerEntity, usedTime: Int) = Unit
}