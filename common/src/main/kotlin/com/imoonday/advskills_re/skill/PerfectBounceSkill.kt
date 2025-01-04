package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class PerfectBounceSkill : BounceSkill(
    Settings(
        id = "perfect_bounce",
        cooldown = 5,
        rarity = SkillRarity.EPIC
    )
) {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(
                name = PARAM_HEAL_RATIO,
                baseValue = DEFAULT_HEAL_RATIO,
                enhancementId = ENHANCEMENT_HEAL_RATIO,
                value = 0.2f,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_KNOCKBACK_FORCE,
                baseValue = DEFAULT_KNOCKBACK_FORCE,
                enhancementId = ENHANCEMENT_FORCE,
                value = 0.1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
        super.initDefaultSettings(settings)
    }

    override fun getDuration(): Int = 2

    override fun getDamageMultiplier(): Float = 1.5f

    override fun getBaseChance(): Float? = null

    override fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?,
    ): Boolean {
        if (!player.isUsing()) return false
        val time = getStartTime(player)?.let {
            System.currentTimeMillis() - it
        }
        player.stopUsing()
        player.stopCooling()
        player.playSoundFromParam(PARAM_BOUNCE_SOUND, DEFAULT_BOUNCE_SOUND)

        val healAmount = amount * getFloatParam(PARAM_HEAL_RATIO, player, DEFAULT_HEAL_RATIO, 0f)
        player.heal(healAmount)

        player.sendMessage(message("success", time?.let { " ${it / 1000.0}s" } ?: ""), true)
        attacker?.run {
            val damage = amount * getFloatParam(PARAM_DAMAGE_BOOST, player, DEFAULT_DAMAGE_BOOST, 0f)
            damage(player.damageSources.thorns(player), damage)

            val force = getDoubleParam(PARAM_KNOCKBACK_FORCE, player, DEFAULT_KNOCKBACK_FORCE)
            velocity = pos.subtract(player.pos).normalize().multiply(force, 0.0, force).add(0.0, 0.5, 0.0)
            velocityDirty = true
            (this as? ServerPlayerEntity)?.updateVelocity()
        }
        return true
    }

    companion object {

        // Default Values
        private const val DEFAULT_HEAL_RATIO = 0.1f
        private const val DEFAULT_KNOCKBACK_FORCE = 1.5
        private const val DEFAULT_DAMAGE_BOOST = 1.5f
        private val DEFAULT_BOUNCE_SOUND = SoundEvents.ITEM_SHIELD_BLOCK

        // Parameter Names
        private const val PARAM_HEAL_RATIO = "heal_ratio"  // 治疗比例
        private const val PARAM_KNOCKBACK_FORCE = "knockback_force"  // 击退力度
        private const val PARAM_DAMAGE_BOOST = "damage_boost"  // 伤害提升
        private const val PARAM_BOUNCE_SOUND = "bounce_sound"  // 反射音效

        // Enhancement IDs
        private const val ENHANCEMENT_HEAL_RATIO = "heal_ratio"  // 对应治疗比例
        private const val ENHANCEMENT_FORCE = "force"  // 对应击退力度
    }
}