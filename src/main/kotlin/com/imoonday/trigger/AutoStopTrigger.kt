package com.imoonday.trigger

import com.imoonday.component.getSkillUsedTime
import com.imoonday.component.stopUsingSkill
import com.imoonday.skill.Skill
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

interface AutoStopTrigger : TickTrigger, UsingProgressTrigger {

    val persistTime: Int
    val skill: Skill

    override fun tick(player: ServerPlayerEntity, usedTime: Int) {
        if (usedTime >= persistTime) {
            onStop(player)
            player.stopUsingSkill(skill)
        }
    }

    fun onStop(player: ServerPlayerEntity) {}

    override fun getProgress(player: PlayerEntity): Double =
        (persistTime - player.getSkillUsedTime(skill)) / persistTime.toDouble()
}