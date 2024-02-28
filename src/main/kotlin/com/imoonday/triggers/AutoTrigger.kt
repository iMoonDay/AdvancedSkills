package com.imoonday.triggers

import com.imoonday.utils.Skill
import com.imoonday.components.startUsingSkill
import net.minecraft.server.network.ServerPlayerEntity

interface AutoTrigger {

    val skill: Skill

    fun shouldStart(player: ServerPlayerEntity): Boolean

    fun tick(player: ServerPlayerEntity) {
        if (shouldStart(player)) {
            player.startUsingSkill(skill)
        }
    }
}