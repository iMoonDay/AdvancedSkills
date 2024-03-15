package com.imoonday.trigger

import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

interface TickTrigger : SkillTrigger {

    fun serverTick(player: ServerPlayerEntity, usedTime: Int) = tick(player, usedTime)

    fun clientTick(player: ClientPlayerEntity, usedTime: Int) = tick(player, usedTime)

    fun tick(player: PlayerEntity, usedTime: Int) = Unit
}