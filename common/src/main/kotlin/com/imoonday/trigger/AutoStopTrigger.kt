package com.imoonday.trigger

import com.imoonday.config.*
import com.imoonday.util.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

interface AutoStopTrigger : TickTrigger, UsingProgressTrigger, UnequipTrigger {

    val persistTime: Int

    fun getPersistTimeModified(): Int {
        val id = getAsSkill().id
        return SkillConfig.instance.skillModifier[id.namespace]?.get(id.path)?.time ?: persistTime
    }

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