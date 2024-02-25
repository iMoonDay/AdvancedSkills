package com.imoonday.trigger

import com.imoonday.components.getSkillData
import com.imoonday.components.lastDamagedTime
import com.imoonday.components.lastReflectedTime
import com.imoonday.components.startUsingSkill
import com.imoonday.utils.UseResult
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Util

interface ReflectionTrigger : AutoStopTrigger {

    fun startReflecting(user: PlayerEntity) = UseResult.of(
        user.startUsingSkill(
            skill,
            NbtCompound().apply { putLong("startTime", Util.getMeasuringTimeMs()) }),
        null,
        Text.translatable("advancedSkills.skill.extreme_reflection.active")
    )

    override fun onStop(player: ServerPlayerEntity) {
        player.lastReflectedTime = Util.getMeasuringTimeMs()
        getStartTime(player)?.let {
            val time = player.lastDamagedTime
            val l = it - time
            if (l < 1000) {
                player.sendMessage(
                    Text.translatable("advancedSkills.skill.extreme_reflection.late", (l / 1000.0).toString()),
                    true
                )
                player.lastDamagedTime = 0
                player.lastReflectedTime = 0
            }
        }
        super.onStop(player)
    }

    fun getStartTime(player: ServerPlayerEntity) = player.getSkillData(skill)?.getLong("startTime")
}