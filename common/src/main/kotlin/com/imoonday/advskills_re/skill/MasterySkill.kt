package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.UseResult
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import kotlin.math.*

class MasterySkill : Skill(
    Settings(
        id = "mastery",
        types = listOf(SkillType.PASSIVE),
        rarity = SkillRarity.LEGENDARY
    )
), CooldownTrigger {

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings.addParameter(
            name = PARAM_COOLDOWN_REDUCTION,
            baseValue = DEFAULT_COOLDOWN_REDUCTION,
            enhancementId = ENHANCEMENT_REDUCTION,
            value = 0.06,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)

    override fun getCooldown(player: PlayerEntity, original: Int): Int {
        val reduction = 1.0 - getDoubleParam(PARAM_COOLDOWN_REDUCTION, player, DEFAULT_COOLDOWN_REDUCTION, max = 1.0)
        return (original * reduction).toInt().coerceAtLeast(min(original, 20))
    }

    companion object {

        // Default Values
        private const val DEFAULT_COOLDOWN_REDUCTION = 0.2

        // Parameter Names
        private const val PARAM_COOLDOWN_REDUCTION = "cooldown_reduction"  // 冷却缩减

        // Enhancement IDs
        private const val ENHANCEMENT_REDUCTION = "reduction"  // 对应冷却缩减
    }
}