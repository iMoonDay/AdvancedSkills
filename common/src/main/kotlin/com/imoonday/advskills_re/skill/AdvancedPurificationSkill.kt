package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.effect.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.text.*

class AdvancedPurificationSkill : Skill(
    id = "advanced_purification",
    types = listOf(SkillType.RESTORATION),
    cooldown = 30,
    rarity = Rarity.SUPERB,
    sound = ModSounds.PURIFY
) {

    override fun use(user: ServerPlayerEntity): UseResult = user.statusEffects
        .filter { it.effectType.category == StatusEffectCategory.HARMFUL && it.duration < 30 * 20 }
        .maxByOrNull { it.duration }
        ?.let {
            user.removeStatusEffect(it.effectType)
            user.spawnParticles(
                ParticleTypes.GLOW_SQUID_INK,
                false, user.centerPos, 10,
                0.25, 0.25, 0.25, 0.1
            )
            return UseResult.success(message("success", Text.translatable(it.translationKey)))
        } ?: user.statusEffects
        .filter { it.effectType.category == StatusEffectCategory.HARMFUL }
        .randomOrNull()
        ?.let { instance ->
            val duration = instance.duration
            instance.setDuration(instance.mapDuration { it - 30 * 20 })
            user.sendPacket(EntityStatusEffectS2CPacket(user.id, instance))
            val amount = (duration - instance.duration) / 20.0
            user.spawnParticles(
                ParticleTypes.GLOW,
                false, user.centerPos, amount.toInt() * 10,
                0.5, 0.5, 0.5, 0.1
            )
            return UseResult.success(
                Skills.PRIMARY_PURIFICATION.message(
                    "success",
                    Text.translatable(instance.translationKey),
                    amount
                )
            )
        }
    ?: UseResult.fail(failedMessage())
}