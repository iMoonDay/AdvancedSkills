package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

abstract class BounceSkill(
    settings: Settings,
    private val duration: Int,
    private val damageBoost: Float = 1.0f,
    private val baseChance: Float? = null
) : Skill(settings), DamageTrigger, BounceTrigger, UsingRenderTrigger {

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings.addTypeToTopIfAbsent(SkillType.DEFENSE)

        settings
            .addParameter(PARAM_BOUNCE_SOUND, DEFAULT_BOUNCE_SOUND)
            .addParameter(
                name = PARAM_BOUNCE_DURATION,
                baseValue = duration,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT,
                genericText = true
            ).addParameter(
                name = PARAM_BOUNCE_BOOST,
                baseValue = damageBoost,
                enhancementId = ENHANCEMENT_BOOST,
                value = 0.1,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT,
                genericText = true
            )

        baseChance?.let {
            settings.addParameter(
                name = PARAM_BOUNCE_CHANCE,
                baseValue = it,
                enhancementId = ENHANCEMENT_CHANCE,
                value = 0.05,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT,
                genericText = true
            )
        }
    }

    override fun use(user: ServerPlayerEntity): UseResult = startBouncing(user)

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_BOUNCE_DURATION, player, duration, 0)

    protected fun bounceFailed(player: ServerPlayerEntity) =
        player.sendMessage(translate("bounce.failed"), true)

    protected fun bounce(
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
        amount: Float,
    ) {
        player.playSoundFromParam(PARAM_BOUNCE_SOUND, DEFAULT_BOUNCE_SOUND)
        val damage = amount * getFloatParam(PARAM_BOUNCE_BOOST, player, damageBoost, 0f)
        attacker?.damage(player.damageSources.thorns(player), damage)?.let {
            player.sendMessage(
                translate("bounce.${if (it) "success" else "failed"}"),
                true
            )
        }
    }

    protected fun ServerPlayerEntity.bounce(
        attacker: LivingEntity?,
        amount: Float,
    ): Boolean {
        val chance = getFloatParam(PARAM_BOUNCE_CHANCE, this, baseChance, 0f, 1f)
        return if (random.nextFloat() < chance) {
            bounce(this, attacker, amount)
            true
        } else {
            bounceFailed(this)
            false
        }
    }

    companion object {

        // Default Values
        private val DEFAULT_BOUNCE_SOUND = SoundEvents.ITEM_SHIELD_BLOCK

        // Parameter Names
        private const val PARAM_BOUNCE_SOUND = "bounce_sound"  // 反弹音效
        private const val PARAM_BOUNCE_DURATION = "bounce_duration"  // 反弹持续时间
        private const val PARAM_BOUNCE_BOOST = "bounce_boost"  // 反弹伤害倍率
        private const val PARAM_BOUNCE_CHANCE = "bounce_chance"  // 反弹成功概率

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "bounce_duration"  // 对应持续时间
        private const val ENHANCEMENT_BOOST = "bounce_boost"  // 对应伤害倍率
        private const val ENHANCEMENT_CHANCE = "bounce_chance"  // 对应成功概率
    }
}