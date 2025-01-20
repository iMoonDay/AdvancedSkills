package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
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

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings
            .addParameter(
                name = PARAM_SHIELD_DURATION,
                baseValue = DEFAULT_SHIELD_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_CHARGE_INTERVAL,
                baseValue = DEFAULT_CHARGE_INTERVAL,
                enhancementId = ENHANCEMENT_INTERVAL,
                value = -2,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = PARAM_MAX_ABSORPTION,
                baseValue = DEFAULT_MAX_ABSORPTION,
                enhancementId = ENHANCEMENT_ABSORPTION,
                value = 2.0f,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.FLOAT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this).withCooling(true)

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_SHIELD_DURATION, player, DEFAULT_SHIELD_DURATION, 0)

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (!player.isUsing()) return
        val interval = getIntParam(PARAM_CHARGE_INTERVAL, player, DEFAULT_CHARGE_INTERVAL)
        val maxAbsorption = getFloatParam(PARAM_MAX_ABSORPTION, player, DEFAULT_MAX_ABSORPTION)
        if (usedTime % min(interval, (getMaxUseTime(player) / maxAbsorption).toInt()).coerceAtLeast(1) == 0) {
            player.absorptionAmount = (player.absorptionAmount + 1).coerceAtMost(maxAbsorption)
        }
    }

    companion object {

        // Default Values
        private const val DEFAULT_SHIELD_DURATION = 10 * 20
        private const val DEFAULT_CHARGE_INTERVAL = 20
        private const val DEFAULT_MAX_ABSORPTION = 10.0f

        // Parameter Names
        private const val PARAM_SHIELD_DURATION = "shield_duration"  // 护盾持续时间
        private const val PARAM_CHARGE_INTERVAL = "charge_interval"  // 充能间隔
        private const val PARAM_MAX_ABSORPTION = "max_absorption"  // 最大吸收量

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
        private const val ENHANCEMENT_INTERVAL = "interval"  // 对应间隔
        private const val ENHANCEMENT_ABSORPTION = "absorption"  // 对应吸收量
    }
}