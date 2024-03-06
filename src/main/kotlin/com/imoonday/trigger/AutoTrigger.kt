package com.imoonday.trigger

import com.imoonday.skill.Skill
import com.imoonday.component.startUsingSkill
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