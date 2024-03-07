package com.imoonday.trigger

import com.imoonday.component.getSkillUsedTime
import com.imoonday.util.SkillSlot
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

interface AutoStopTrigger : TickTrigger, UsingProgressTrigger, UnequipTrigger {

    fun getPersistTime(): Int

    override fun tick(player: ServerPlayerEntity, usedTime: Int) {
        if (usedTime >= getPersistTime()) {
            onStop(player)
            player.stopUsing()
        }
    }

    fun onStop(player: ServerPlayerEntity) = Unit

    override fun getProgress(player: PlayerEntity): Double =
        (getPersistTime() - player.getSkillUsedTime(asSkill())) / getPersistTime().toDouble()

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        onStop(player)
        super.postUnequipped(player, slot)
    }
}