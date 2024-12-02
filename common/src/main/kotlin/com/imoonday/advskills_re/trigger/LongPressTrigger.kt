package com.imoonday.advskills_re.trigger

import com.imoonday.advskills_re.network.c2s.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

interface LongPressTrigger : TickTrigger, AutoStopTrigger {

    fun getMaxPressTime(): Int

    override val persistTime: Int
        get() = getMaxPressTime()

    fun use(player: ServerPlayerEntity, keyState: UseSkillC2SRequest.KeyState): UseResult =
        if (keyState == UseSkillC2SRequest.KeyState.PRESS) onPress(player)
        else onRelease(player, player.getUsedTime())

    fun onPress(player: ServerPlayerEntity): UseResult {
        player.startUsing()
        return getChargingResult()
    }

    fun getChargingResult() = UseResult.fail(translate("useSkill.charging", getAsSkill().name))

    fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult

    override fun onStop(player: ServerPlayerEntity) {
        val result = onRelease(player, getMaxPressTime())
        getAsSkill().handleResult(player, result)
        super.onStop(player)
    }

    override fun getProgress(player: PlayerEntity): Double = 1.0 - super.getProgress(player)
}