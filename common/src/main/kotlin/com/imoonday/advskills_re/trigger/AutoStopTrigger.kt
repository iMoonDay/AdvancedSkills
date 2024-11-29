package com.imoonday.advskills_re.trigger

import com.imoonday.advskills_re.config.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.world.*

interface AutoStopTrigger : TickTrigger, UsingProgressTrigger, UnequipTrigger {

    val persistTime: Int

    fun getPersistTimeModified(world: World? = null): Int {
        val id = getAsSkill().id
        return (world?.skillConfig ?: SkillConfig.instance).getModifier(id)?.time ?: persistTime
    }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (usedTime >= getPersistTimeModified(player.world)) {
            onStop(player)
            player.stopUsing()
        }
    }

    fun onStop(player: ServerPlayerEntity) = Unit

    override fun getProgress(player: PlayerEntity): Double {
        val time = getPersistTimeModified(player.world)
        return (time - player.getUsedTime()) / time.toDouble()
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        onStop(player)
        super.postUnequipped(player, slot)
    }
}