package com.imoonday.advskills_re.skill.trigger

import com.imoonday.advskills_re.network.c2s.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

interface LongPressTrigger : TickTrigger, AutoStopTrigger {

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
        val result = onRelease(player, getPersistTime(player))
        getAsSkill().handleResult(player, result)
        super.onStop(player)
    }

    override fun getProgress(player: PlayerEntity): Double = (1.0 - super.getProgress(player)).coerceIn(0.0, 1.0)

    fun alwaysKeepCharging(player: PlayerEntity): Boolean = false

    override fun canAutoStop(player: PlayerEntity): Boolean = !alwaysKeepCharging(player) && super.canAutoStop(player)
}