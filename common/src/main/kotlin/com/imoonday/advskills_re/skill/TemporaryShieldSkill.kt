package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*
import kotlin.math.*

class TemporaryShieldSkill : Skill(
    id = "temporary_shield",
    types = listOf(SkillType.DEFENSE),
    cooldown = 30,
    rarity = SkillRarity.LEGENDARY,
    enhancements = setOf(
        SkillEnhancements.PERSISTENT_TIME,
        SkillEnhancements.EFFECT_FREQUENCY,
        SkillEnhancements.EFFECT_VALUE
    )
), AutoStopTrigger {

    init {
        addEnhanceableParameter(
            name = timeParamName,
            baseValue = 10 * 20,
            enhancementId = "time",
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5
        ) { (it * 100).toInt() }
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this).withCooling(true)

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (!player.isUsing()) return
        val frequency = (20 - player.getEnhancementLvl(SkillEnhancements.EFFECT_FREQUENCY) * 2).coerceAtLeast(1)
        val maxValue = 10f + player.getEnhancementLvl(SkillEnhancements.EFFECT_VALUE) * 2f
        if (usedTime % min(frequency, (getPersistTime(player) / maxValue).toInt()) == 0) {
            player.absorptionAmount =
                (player.absorptionAmount + 1).coerceAtMost(maxValue)
        }
    }
}