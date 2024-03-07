package com.imoonday.trigger

import com.imoonday.component.startUsingSkill
import com.imoonday.util.UseResult
import com.imoonday.util.translateSkill
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

interface LongPressTrigger : TickTrigger, AutoStopTrigger {

    fun getMaxPressTime(): Int
    override fun getPersistTime(): Int = getMaxPressTime()

    fun onPress(player: ServerPlayerEntity): UseResult {
        player.startUsingSkill(asSkill())
        return UseResult.fail(translateSkill("charged_sweep", "charging", asSkill().name.string))
    }

    fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult

    override fun onStop(player: ServerPlayerEntity) {
        val result = onRelease(player, getMaxPressTime())
        asSkill().handleResult(player, result)
        super.onStop(player)
    }

    override fun getProgress(player: PlayerEntity): Double = 1.0 - super.getProgress(player)
}