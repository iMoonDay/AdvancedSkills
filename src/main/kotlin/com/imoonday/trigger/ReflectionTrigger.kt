package com.imoonday.trigger

import com.imoonday.component.getSkillData
import com.imoonday.component.lastDamagedTime
import com.imoonday.component.lastReflectedTime
import com.imoonday.util.UseResult
import com.imoonday.util.translateSkill
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Util

interface ReflectionTrigger : AutoStopTrigger {

    fun startReflecting(user: PlayerEntity) = UseResult.of(
        user.startUsing { it.putLong("startTime", Util.getMeasuringTimeMs()) },
        null,
        translateSkill("extreme_reflection", "active")
    )

    override fun onStop(player: ServerPlayerEntity) {
        player.lastReflectedTime = Util.getMeasuringTimeMs()
        getStartTime(player)?.let {
            val time = player.lastDamagedTime
            val l = it - time
            if (l < 1000) {
                player.sendMessage(
                    translateSkill("extreme_reflection", "late", (l / 1000.0).toString()),
                    true
                )
                player.lastDamagedTime = 0
                player.lastReflectedTime = 0
            }
        }
        super.onStop(player)
    }

    fun getStartTime(player: ServerPlayerEntity) = player.getSkillData(asSkill())?.getLong("startTime")
}