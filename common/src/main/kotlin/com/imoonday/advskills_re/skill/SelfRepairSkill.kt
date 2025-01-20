package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.item.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class SelfRepairSkill : Skill(
    Settings(
        id = "self_repair",
        types = listOf(SkillType.PASSIVE),
        rarity = SkillRarity.SUPERB,
        cooldown = 10
    )
), AutoTrigger, AutoStopTrigger {

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings
            .addParameter(
                name = PARAM_CHARGE_DURATION,
                baseValue = DEFAULT_CHARGE_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = -0.16,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_REPAIR_THRESHOLD,
                baseValue = DEFAULT_REPAIR_THRESHOLD,
                enhancementId = ENHANCEMENT_THRESHOLD,
                value = 0.1f,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)

    override fun shouldStart(player: ServerPlayerEntity): Boolean =
        player.armorItems.filter { it.isDamaged }.any { it.damage > getMaxRepairLimit(player, it) }

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_CHARGE_DURATION, player, DEFAULT_CHARGE_DURATION, 0)

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        if (!player.hasEquipped()) return

        val repaired = player.armorItems
            .filter { it.isDamaged && it.damage > getMaxRepairLimit(player, it) }
            .count {
                it.damage -= 1
                true
            }
        if (repaired > 0) {
            player.spawnParticles(
                ParticleTypes.COMPOSTER, false, player.centerPos, repaired, 0.5, 0.5, 0.5, 0.1
            )
        }
    }

    fun getMaxRepairLimit(player: PlayerEntity, stack: ItemStack): Int {
        val threshold = 1.0f - getFloatParam(PARAM_REPAIR_THRESHOLD, player, DEFAULT_REPAIR_THRESHOLD, max = 1.0f)
        return (stack.maxDamage * threshold).toInt().coerceAtLeast(0)
    }

    override fun shouldFlashIcon(player: PlayerEntity): Boolean = false

    override fun getProgress(player: PlayerEntity): Double = 1.0 - super.getProgress(player)

    companion object {

        // Default Values
        private const val DEFAULT_CHARGE_DURATION = 10 * 20
        private const val DEFAULT_REPAIR_THRESHOLD = 0.5f

        // Parameter Names
        private const val PARAM_CHARGE_DURATION = "charge_duration"  // 充能时间
        private const val PARAM_REPAIR_THRESHOLD = "repair_threshold"  // 修复阈值

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
        private const val ENHANCEMENT_THRESHOLD = "threshold"  // 对应阈值
    }
}