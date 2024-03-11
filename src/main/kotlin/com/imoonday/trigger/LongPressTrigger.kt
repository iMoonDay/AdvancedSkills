package com.imoonday.trigger

import com.imoonday.component.getSkillUsedTime
import com.imoonday.network.UseSkillC2SRequest
import com.imoonday.util.UseResult
import com.imoonday.util.translate
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

interface LongPressTrigger : TickTrigger, AutoStopTrigger {

    fun getMaxPressTime(): Int
    override fun getPersistTime(): Int = getMaxPressTime()

    fun use(player: ServerPlayerEntity, keyState: UseSkillC2SRequest.KeyState): UseResult =
        if (keyState == UseSkillC2SRequest.KeyState.PRESS) onPress(player)
        else onRelease(player, player.getSkillUsedTime(asSkill()))

    fun onPress(player: ServerPlayerEntity): UseResult {
        player.startUsing()
        return UseResult.fail(translate("useSkill", "charging", asSkill().name.string))
    }

    fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult

    override fun onStop(player: ServerPlayerEntity) {
        val result = onRelease(player, getMaxPressTime())
        asSkill().handleResult(player, result)
        super.onStop(player)
    }

    override fun getProgress(player: PlayerEntity): Double = 1.0 - super.getProgress(player)
}