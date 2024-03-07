package com.imoonday.skill

import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.BreatheInWaterTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.server.network.ServerPlayerEntity

class WaterBreathingSkill : Skill(
    id = "water_breathing",
    types = arrayOf(SkillType.ENHANCEMENT),
    cooldown = 10,
    rarity = Rarity.RARE,
), AutoStopTrigger, BreatheInWaterTrigger {
    override fun getPersistTime(): Int = 20 * 30

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this).withCooling(false)

    override fun onStop(player: ServerPlayerEntity) {
        player.startCooling()
        super.onStop(player)
    }
}