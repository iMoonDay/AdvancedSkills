package com.imoonday.advskills_re.trigger

import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

interface AutoStopTrigger : TickTrigger, UsingProgressTrigger, UnequipTrigger {

    val persistTime: Int

    fun getPersistTimeModified(): Int = SkillConfig.get().getModifier(getAsSkill().id)?.time ?: persistTime

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (usedTime >= getPersistTimeModified()) {
            onStop(player)
            player.stopUsing()
        }
    }

    fun onStop(player: ServerPlayerEntity) = Unit

    override fun getProgress(player: PlayerEntity): Double {
        val time = getPersistTimeModified()
        return (time - player.getUsedTime()) / time.toDouble()
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        onStop(player)
        super.postUnequipped(player, slot)
    }
}