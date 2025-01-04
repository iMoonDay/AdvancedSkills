package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

class MicroBounceSkill : BounceSkill(
    Settings(
        id = "micro_bounce",
        cooldown = 6,
        rarity = SkillRarity.RARE
    )
) {

    override fun initDefaultSettings(settings: Settings) {
        settings.addParameter(
            name = PARAM_DAMAGE_REDUCTION,
            baseValue = DEFAULT_DAMAGE_REDUCTION,
            enhancementId = ENHANCEMENT_REDUCTION,
            value = 0.05f,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT
        )
        super.initDefaultSettings(settings)
    }

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float {
        if (!player.isUsing()) return amount
        player.stopUsing()
        player.bounce(attacker, amount / 2)
        return amount * (1f - getFloatParam(PARAM_DAMAGE_REDUCTION, player, DEFAULT_DAMAGE_REDUCTION, max = 1f))
    }

    override fun getDuration(): Int = 20

    override fun getDamageMultiplier(): Float = 1.0f

    override fun getBaseChance(): Float = 0.25f

    companion object {

        // Default Values
        private const val DEFAULT_DAMAGE_REDUCTION = 0.25f

        // Parameter Names
        private const val PARAM_DAMAGE_REDUCTION = "damage_reduction"  // 伤害减免

        // Enhancement IDs
        private const val ENHANCEMENT_REDUCTION = "reduction"  // 对应伤害减免
    }
}