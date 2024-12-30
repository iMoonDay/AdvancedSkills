package com.imoonday.advskills_re.skill.trigger

import com.imoonday.advskills_re.component.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

interface AutoStopTrigger : TickTrigger, UsingProgressTrigger, UnequipTrigger {

    val timeParamName: String get() = "persist_time"

    fun getPersistTime(player: PlayerEntity): Int =
        getIntParam(timeParamName, player, 0, 0)

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (canAutoStop(player) && usedTime >= getPersistTime(player)) {
            onStop(player)
            player.stopUsing()
        }
    }

    fun onStop(player: ServerPlayerEntity) = Unit

    fun canAutoStop(player: PlayerEntity): Boolean = true

    override fun getProgress(player: PlayerEntity): Double {
        val time = getPersistTime(player)
        return (time - player.getUsedTime()) / time.toDouble()
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        super.postUnequipped(player, slot)
        onStop(player)
    }
}