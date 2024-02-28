package com.imoonday.triggers

import com.imoonday.components.startUsingSkill
import com.imoonday.utils.UseResult
import com.imoonday.utils.translateSkill
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
}