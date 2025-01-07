package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.effect.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.text.*
import kotlin.math.*

class PrimaryPurificationSkill : Skill(
    Settings(
        id = "primary_purification",
        types = listOf(SkillType.RESTORATION),
        cooldown = 30,
        rarity = SkillRarity.SUPERB
    )
) {

    init {
        settings
            .addParameter(PARAM_PURIFY_SOUND, DEFAULT_PURIFY_SOUND)
            .addParameter(
                name = PARAM_PURIFY_DURATION,
                baseValue = DEFAULT_PURIFY_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = user.statusEffects
        .filter { it.effectType.category == StatusEffectCategory.HARMFUL }
        .randomOrNull()
        ?.let { effect ->
            val originalDuration = effect.duration
            val maxDuration = getIntParam(PARAM_PURIFY_DURATION, user, DEFAULT_PURIFY_DURATION).toDouble()
            effect.setDuration(effect.mapDuration { (it - min(it * 0.2, maxDuration)).toInt() })
            user.sendPacket(EntityStatusEffectS2CPacket(user.id, effect))
            
            val reducedSeconds = (originalDuration - effect.duration) / 20.0
            user.spawnParticles(
                ParticleTypes.GLOW,
                false, user.centerPos, (reducedSeconds.toInt() * 10).coerceAtLeast(1),
                0.5, 0.5, 0.5, 0.1
            )
            return UseResult.success(
                message(
                    "success",
                    Text.translatable(effect.translationKey),
                    reducedSeconds
                ),
                getSoundEventParam(PARAM_PURIFY_SOUND, DEFAULT_PURIFY_SOUND.get())
            )
        } ?: UseResult.fail(failedMessage)

    companion object {
        // Default Values
        private const val DEFAULT_PURIFY_DURATION = 15 * 20
        private val DEFAULT_PURIFY_SOUND = ModSounds.PURIFY

        // Parameter Names
        private const val PARAM_PURIFY_SOUND = "purify_sound"  // 净化音效
        private const val PARAM_PURIFY_DURATION = "purify_duration"  // 净化持续时间

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
    }
}