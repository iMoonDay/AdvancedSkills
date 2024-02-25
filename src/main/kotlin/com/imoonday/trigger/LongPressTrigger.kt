package com.imoonday.trigger

import com.imoonday.components.startUsingSkill
import com.imoonday.utils.UseResult
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

interface LongPressTrigger : TickTrigger, AutoStopTrigger {

    val maxPressTime: Int
    override val persistTime: Int
        get() = maxPressTime

    fun onPress(player: ServerPlayerEntity): UseResult {
        player.startUsingSkill(skill)
        return UseResult.fail(Text.literal("${skill.name.string} start charging"))
    }

    fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult

    override fun onStop(player: ServerPlayerEntity) {
        onRelease(player, maxPressTime)
        super.onStop(player)
    }
}