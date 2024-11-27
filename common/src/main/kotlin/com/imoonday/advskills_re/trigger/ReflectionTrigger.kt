package com.imoonday.advskills_re.trigger

import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*

interface ReflectionTrigger : AutoStopTrigger {

    fun startReflecting(user: ServerPlayerEntity): UseResult {
        val startTime = System.currentTimeMillis()
        return UseResult.of(
            user.startUsing { it.putLong("startTime", startTime) },
            null,
            translate("reflection.active")
        )
    }

    override fun onStop(player: ServerPlayerEntity) {
        player.lastReflectedTime = System.currentTimeMillis()
        getStartTime(player)?.let {
            val time = player.lastDamagedTime
            val l = it - time
            if (l < 1000) {
                player.sendMessage(
                    translate("reflection.late", (l / 1000.0).toString()),
                    true
                )
                player.lastDamagedTime = 0
                player.lastReflectedTime = 0
            }
        }
        super.onStop(player)
    }

    fun getStartTime(player: ServerPlayerEntity) = player.getUsingData()?.getLong("startTime")
}