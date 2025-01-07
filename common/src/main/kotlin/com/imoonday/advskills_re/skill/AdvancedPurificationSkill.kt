package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.effect.*
import net.minecraft.entity.player.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.text.*

class AdvancedPurificationSkill : Skill(
    Settings(
        id = "advanced_purification",
        types = listOf(SkillType.RESTORATION),
        cooldown = 30,
        rarity = SkillRarity.SUPERB
    )
) {

    init {
        settings
            .addParameter(PARAM_SUCCESS_SOUND, DEFAULT_SUCCESS_SOUND)
            .addParameter(
                name = PARAM_MAX_DURATION,
                baseValue = DEFAULT_MAX_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = user.statusEffects
        .filter { it.effectType.category == StatusEffectCategory.HARMFUL && it.duration < getTimeUpLimit(user) }
        .maxByOrNull { it.duration }
        ?.let {
            user.removeStatusEffect(it.effectType)
            user.spawnParticles(
                ParticleTypes.GLOW_SQUID_INK,
                false, user.centerPos, 10,
                0.25, 0.25, 0.25, 0.1
            )
            UseResult.success(message("success", Text.translatable(it.translationKey))).withSound(getSuccessSound())
        } ?: user.statusEffects
        .filter { it.effectType.category == StatusEffectCategory.HARMFUL }
        .randomOrNull()
        ?.let { instance ->
            val duration = instance.duration
            instance.setDuration(instance.mapDuration { it - getTimeUpLimit(user) })
            user.sendPacket(EntityStatusEffectS2CPacket(user.id, instance))
            val amount = (duration - instance.duration) / 20.0
            user.spawnParticles(
                ParticleTypes.GLOW,
                false, user.centerPos, amount.toInt() * 10,
                0.5, 0.5, 0.5, 0.1
            )
            UseResult.success(
                Skills.PRIMARY_PURIFICATION.message(
                    "success",
                    Text.translatable(instance.translationKey),
                    amount
                )
            ).withSound(getSuccessSound())
        }
    ?: UseResult.fail(failedMessage)

    private fun getSuccessSound() = getSoundEventParam(PARAM_SUCCESS_SOUND, DEFAULT_SUCCESS_SOUND.get())

    private fun getTimeUpLimit(player: PlayerEntity) =
        getIntParam(PARAM_MAX_DURATION, player, DEFAULT_MAX_DURATION, 0)

    companion object {

        // Default Values
        private const val DEFAULT_MAX_DURATION = 30 * 20
        private val DEFAULT_SUCCESS_SOUND = ModSounds.PURIFY

        // Parameter Names
        private const val PARAM_SUCCESS_SOUND = "success_sound"  // 成功音效
        private const val PARAM_MAX_DURATION = "max_removable_duration"  // 最大可净化持续时间

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应最大净化时间
    }
}