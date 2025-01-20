package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.trigger.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

class PainFeedbackSkill : PassiveSkill(
    Settings(
        id = "pain_feedback",
        cooldown = 5,
        rarity = SkillRarity.SUPERB
    ), customToggles = true
), PostDamagedTrigger {

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings.addParameter(
            name = PARAM_FEEDBACK_RATIO,
            baseValue = DEFAULT_FEEDBACK_RATIO,
            enhancementId = ENHANCEMENT_RATIO,
            value = 0.2f,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT
        )
    }

    override fun postDamaged(amount: Float, source: DamageSource, player: ServerPlayerEntity, attacker: LivingEntity?) {
        super.postDamaged(amount, source, player, attacker)
        if (!isAvailable(player)) return

        if (attacker == null || player.isCooling()) return
        if (amount > 0f) {
            val ratio = getFloatParam(PARAM_FEEDBACK_RATIO, player, DEFAULT_FEEDBACK_RATIO)
            val damage = amount * ratio
            attacker.damage(player.damageSources.thorns(player), damage)
            player.startCooling()
        }
    }

    companion object {

        // Default Values
        private const val DEFAULT_FEEDBACK_RATIO = 0.5f

        // Parameter Names
        private const val PARAM_FEEDBACK_RATIO = "feedback_ratio"  // 反馈比例

        // Enhancement IDs
        private const val ENHANCEMENT_RATIO = "ratio"  // 对应反馈比例
    }
}