package com.imoonday.trigger

import com.imoonday.component.startUsingSkill
import com.imoonday.util.UseResult
import com.imoonday.util.translateSkill
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

interface LongPressTrigger : TickTrigger, AutoStopTrigger {

    val maxPressTime: Int
    override val persistTime: Int
        get() = maxPressTime

    fun onPress(player: ServerPlayerEntity): UseResult {
        player.startUsingSkill(skill)
        return UseResult.fail(translateSkill("charged_sweep", "charging", skill.name.string))
    }

    fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult

    override fun onStop(player: ServerPlayerEntity) {
        val result = onRelease(player, maxPressTime)
        skill.handleResult(player, result)
        super.onStop(player)
    }

    override fun getProgress(player: PlayerEntity): Double = 1.0 - super.getProgress(player)
}