package com.imoonday.advskills_re.skill.trigger

import com.imoonday.advskills_re.component.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

interface AutoStopTrigger : TickTrigger, UsingProgressTrigger, UnequipTrigger {

    fun getMaxUseTime(player: PlayerEntity): Int

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (canAutoStop(player) && usedTime >= getMaxUseTime(player)) {
            onStop(player)
            player.stopUsing()
        }
    }

    fun onStop(player: ServerPlayerEntity) = Unit

    fun canAutoStop(player: PlayerEntity): Boolean = true

    override fun getProgress(player: PlayerEntity): Double {
        val time = getMaxUseTime(player)
        return (time - player.getUsedTime()) / time.toDouble()
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        super.postUnequipped(player, slot)
        onStop(player)
    }

    companion object {

        const val PERSIST_TIME = "persist_time"
        const val CHARGE_TIME = "charge_time"
    }
}