package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.effect.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.util.*
import net.minecraft.util.hit.*

class PrimaryConfinementSkill : LongPressSkill(
    Settings(
        id = "primary_confinement",
        types = listOf(SkillType.CONTROL),
        cooldown = 12,
        rarity = SkillRarity.SUPERB
    )
), UsingRenderTrigger, CrosshairTrigger, TargetRenderTrigger {

    init {
        settings
            .addParameter(PARAM_CONFINE_SOUND, DEFAULT_CONFINE_SOUND)
            .addParameter(
                name = PARAM_CHARGE_DURATION,
                baseValue = DEFAULT_CHARGE_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = -0.16,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT,
                genericText = true
            ).addParameter(
                name = PARAM_CONFINE_RANGE,
                baseValue = DEFAULT_CONFINE_RANGE,
                enhancementId = ENHANCEMENT_RANGE,
                value = 1.0,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = PARAM_SUCCESS_CHANCE,
                baseValue = DEFAULT_SUCCESS_CHANCE,
                enhancementId = ENHANCEMENT_CHANCE,
                value = 0.04f,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_EFFECT_DURATION,
                baseValue = DEFAULT_EFFECT_DURATION,
                enhancementId = ENHANCEMENT_EFFECT,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopAndCooldown()
        player.swingHand(Hand.MAIN_HAND, true)
        player.raycastLivingEntity(getRange(player))?.takeIf { it.type == HitResult.Type.ENTITY }?.let {
            val chance = getFloatParam(PARAM_SUCCESS_CHANCE, player, DEFAULT_SUCCESS_CHANCE, max = 1.0f)
            if (player.random.nextFloat() < chance * pressedTime / getMaxUseTime(player)) {
                val duration = getIntParam(PARAM_EFFECT_DURATION, player, DEFAULT_EFFECT_DURATION)
                (it.entity as LivingEntity).addStatusEffect(
                    StatusEffectInstance(ModEffects.CONFINEMENT.get(), duration, 0, false, false, true)
                )
                return UseResult.success(
                    message("success"),
                    getSoundEventParam(PARAM_CONFINE_SOUND, DEFAULT_CONFINE_SOUND)
                )
            }
            return UseResult.pass(failedMessage)
        }
        return UseResult.pass(message("empty"))
    }

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_CHARGE_DURATION, player, DEFAULT_CHARGE_DURATION, 0)

    override fun getCrosshair(player: PlayerEntity): Crosshair {
        player.run {
            if (!isUsing()) return Crosshairs.NONE
            if (raycastLivingEntity(getRange(player))?.type == HitResult.Type.ENTITY) return Crosshairs.CROSS
        }
        return Crosshairs.NONE
    }

    override fun isTarget(clientPlayer: PlayerEntity, entity: LivingEntity): Boolean {
        if (!clientPlayer.isUsing()) return false
        return clientPlayer.raycastLivingEntity(getRange(clientPlayer))?.entity == entity
    }

    override fun shouldRenderPostLiving(living: LivingEntity, player: PlayerEntity): Boolean =
        super<TargetRenderTrigger>.shouldRenderPostLiving(living, player)

    private fun getRange(player: PlayerEntity): Double =
        getDoubleParam(PARAM_CONFINE_RANGE, player, DEFAULT_CONFINE_RANGE)

    companion object {

        // Default Values
        private const val DEFAULT_CHARGE_DURATION = 5 * 20
        private const val DEFAULT_CONFINE_RANGE = 5.0
        private const val DEFAULT_SUCCESS_CHANCE = 0.8f
        private const val DEFAULT_EFFECT_DURATION = 3 * 20
        private val DEFAULT_CONFINE_SOUND = SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE

        // Parameter Names
        private const val PARAM_CONFINE_SOUND = "confine_sound"  // 禁锢音效
        private const val PARAM_CHARGE_DURATION = "charge_duration"  // 蓄力时间
        private const val PARAM_CONFINE_RANGE = "confine_range"  // 禁锢范围
        private const val PARAM_SUCCESS_CHANCE = "success_chance"  // 成功概率
        private const val PARAM_EFFECT_DURATION = "effect_duration"  // 效果持续时间

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "charge_time"  // 对应蓄力时间
        private const val ENHANCEMENT_RANGE = "range"  // 对应范围
        private const val ENHANCEMENT_CHANCE = "chance"  // 对应概率
        private const val ENHANCEMENT_EFFECT = "effect"  // 对应效果持续时间
    }
}