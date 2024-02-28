package com.imoonday.triggers

import com.imoonday.components.stopUsingSkill
import com.imoonday.utils.Skill
import net.minecraft.server.network.ServerPlayerEntity

interface AutoStopTrigger : TickTrigger {

    val persistTime: Int
    val skill: Skill

    override fun tick(player: ServerPlayerEntity, usedTime: Int) {
        if (usedTime >= persistTime) {
            onStop(player)
            player.stopUsingSkill(skill)
        }
    }

    fun onStop(player: ServerPlayerEntity) {}
}