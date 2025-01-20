package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.effect.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class WitheringTouchSkill : Skill(
    Settings(
        id = "withering_touch",
        types = listOf(SkillType.ENHANCEMENT),
        cooldown = 20,
        rarity = SkillRarity.EPIC
    )
), UsingProgressTrigger, PostAttackTrigger, DangerTrigger, UnequipTrigger {

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings
            .addParameter(PARAM_MAX_EFFECT_DURATION, DEFAULT_MAX_EFFECT_DURATION)
            .addParameter(PARAM_TOUCH_SOUND, DEFAULT_TOUCH_SOUND)
            .addParameter(
                name = PARAM_MAX_TOUCH_COUNT,
                baseValue = DEFAULT_MAX_TOUCH_COUNT,
                enhancementId = ENHANCEMENT_COUNT,
                value = 1.0,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = PARAM_WITHER_DURATION,
                baseValue = DEFAULT_WITHER_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_WITHER_LEVEL,
                baseValue = DEFAULT_WITHER_LEVEL,
                enhancementId = ENHANCEMENT_LEVEL,
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun postAttack(source: DamageSource, player: ServerPlayerEntity, target: LivingEntity) {
        super.postAttack(source, player, target)
        if (!player.isUsing() || source.isIndirect) return

        val baseDuration = getIntParam(PARAM_WITHER_DURATION, player, DEFAULT_WITHER_DURATION)
        val maxDuration = getIntParam(PARAM_MAX_EFFECT_DURATION, player, DEFAULT_MAX_EFFECT_DURATION)
        val baseLevel = getIntParam(PARAM_WITHER_LEVEL, player, DEFAULT_WITHER_LEVEL)
        val (duration, level) = target.getStatusEffect(StatusEffects.WITHER)
            ?.let { (it.duration + baseDuration).coerceAtMost(maxDuration) to it.amplifier + baseLevel }
            ?: (baseDuration to baseLevel)
        target.addStatusEffect(StatusEffectInstance(StatusEffects.WITHER, duration, level))
        val touchSound = getSoundEventParam(PARAM_TOUCH_SOUND, DEFAULT_TOUCH_SOUND)
        if (touchSound != null) {
            target.playSoundIfNotSilent(touchSound)
        }

        val data = player.getActiveData()
        data.putInt(NBT_TOUCH_COUNT, data.getInt(NBT_TOUCH_COUNT) + 1)
        if (data.getInt(NBT_TOUCH_COUNT) >= getIntParam(PARAM_MAX_TOUCH_COUNT, player, DEFAULT_MAX_TOUCH_COUNT)) {
            player.stopAndCooldown()
        } else {
            player.syncData()
        }
    }

    override fun getProgress(player: PlayerEntity): Double =
        1 - player.getActiveData().getInt(NBT_TOUCH_COUNT) / getDoubleParam(
            PARAM_MAX_TOUCH_COUNT, player, DEFAULT_MAX_TOUCH_COUNT.toDouble()
        )

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        super.postUnequipped(player, slot)
        if (player.isUsing()) {
            player.startCooling()
        }
    }

    companion object {

        // Default Values
        private const val DEFAULT_MAX_EFFECT_DURATION = 20 * 9
        private const val DEFAULT_MAX_TOUCH_COUNT = 3
        private const val DEFAULT_WITHER_DURATION = 3 * 20
        private const val DEFAULT_WITHER_LEVEL = 0
        private val DEFAULT_TOUCH_SOUND = SoundEvents.ENTITY_WITHER_SKELETON_HURT

        // Parameter Names
        private const val PARAM_MAX_EFFECT_DURATION = "max_effect_duration"  // 最大效果持续时间
        private const val PARAM_TOUCH_SOUND = "touch_sound"  // 触碰音效
        private const val PARAM_MAX_TOUCH_COUNT = "max_touch_count"  // 最大触碰次数
        private const val PARAM_WITHER_DURATION = "wither_duration"  // 凋零持续时间
        private const val PARAM_WITHER_LEVEL = "wither_level"  // 凋零等级

        // Enhancement IDs
        private const val ENHANCEMENT_COUNT = "count"  // 对应次数
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
        private const val ENHANCEMENT_LEVEL = "level"  // 对应等级

        // NBT Keys
        private const val NBT_TOUCH_COUNT = "TouchCount"  // 触碰次数
    }
}