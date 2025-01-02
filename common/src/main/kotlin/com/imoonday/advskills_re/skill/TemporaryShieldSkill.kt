package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*
import kotlin.math.*

class TemporaryShieldSkill : Skill(
    Settings(
        id = "temporary_shield",
        types = listOf(SkillType.DEFENSE),
        cooldown = 30,
        rarity = SkillRarity.LEGENDARY
    )
), AutoStopTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(
                name = timeParamName,
                baseValue = 10 * 20,
                enhancementId = "time",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "shield_interval",
                baseValue = 20,
                enhancementId = "interval",
                value = -2,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = "max_shield",
                baseValue = 10.0f,
                enhancementId = "value",
                value = 2.0f,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.FLOAT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this).withCooling(true)

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (!player.isUsing()) return
        val interval = getIntParam("shield_interval", player, 20)
        val maxShield = getFloatParam("max_shield", player, 10.0f)
        if (usedTime % min(interval, (getPersistTime(player) / maxShield).toInt()).coerceAtLeast(1) == 0) {
            player.absorptionAmount = (player.absorptionAmount + 1).coerceAtMost(maxShield)
        }
    }
}