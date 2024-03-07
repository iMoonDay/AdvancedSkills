package com.imoonday.trigger

import net.minecraft.server.network.ServerPlayerEntity

interface AutoTrigger : SkillTrigger {

    fun shouldStart(player: ServerPlayerEntity): Boolean

    fun shouldStop(player: ServerPlayerEntity): Boolean = false

    fun tick(player: ServerPlayerEntity) {
        if (shouldStart(player) && !player.isUsing() && !shouldStop(player)) {
            player.startUsing()
        }
        if (player.isUsing() && shouldStop(player)) {
            player.stopUsing()
        }
    }
}