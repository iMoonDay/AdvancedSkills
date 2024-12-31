package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.item.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class SelfRepairSkill : Skill(
    id = "self_repair",
    types = listOf(SkillType.PASSIVE),
    rarity = SkillRarity.SUPERB,
    cooldown = 10,
    enhancements = setOf(SkillEnhancements.CHARGE_TIME, SkillEnhancements.EFFECT_VALUE)
), AutoTrigger, AutoStopTrigger {

    override val timeParamName: String = "charge_time"

    init {
        addParameter(
            name = timeParamName,
            baseValue = 10 * 20,
            enhancementId = "time",
            value = -0.16,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)

    override fun shouldStart(player: ServerPlayerEntity): Boolean =
        player.armorItems.filter { it.isDamaged }.any { it.damage > getMaxRepairLimit(player, it) }

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        if (!player.hasEquipped()) return

        val repaired = player.armorItems.filter { it.isDamaged && it.damage > getMaxRepairLimit(player, it) }
            .count {
                it.damage -= 1
                true
            }
        if (repaired > 0) {
            player.spawnParticles(
                ParticleTypes.COMPOSTER,
                false, player.centerPos, repaired,
                0.5, 0.5, 0.5, 0.1
            )
        }
    }

    fun getMaxRepairLimit(player: PlayerEntity, stack: ItemStack): Int =
        (stack.maxDamage * (0.5 - player.getEnhancementLvl(SkillEnhancements.EFFECT_VALUE) * 0.1)).toInt()
            .coerceAtLeast(0)

    override fun shouldFlashIcon(player: PlayerEntity): Boolean = false

    override fun getProgress(player: PlayerEntity): Double = 1.0 - super.getProgress(player)
}