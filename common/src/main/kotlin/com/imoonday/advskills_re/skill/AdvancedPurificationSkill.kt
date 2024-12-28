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
        this.settings.addParameter("success_sound", ModSounds.PURIFY)
        addEnhanceableParameter(
            name = "max_removal_time",
            baseValue = 30 * 20,
            enhancementId = "time",
            value = 0.2f,
            operation = Enhancement.Operation.MULTIPLY,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
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
    ?: UseResult.fail(failedMessage())

    private fun getSuccessSound() = getSoundEventParam("success_sound")

    private fun getTimeUpLimit(player: PlayerEntity) =
        player.getIntParam("max_removal_time", 30 * 20, 0)
}