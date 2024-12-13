package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.effect.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.text.*
import kotlin.math.*

class PrimaryPurificationSkill : Skill(
    id = "primary_purification",
    types = listOf(SkillType.RESTORATION),
    cooldown = 30,
    rarity = SkillRarity.SUPERB,
    sound = ModSounds.PURIFY,
    enhancements = setOf(SkillEnhancements.TIME_UP_LIMIT)
) {

    override fun use(user: ServerPlayerEntity): UseResult = user.statusEffects
        .filter { it.effectType.category == StatusEffectCategory.HARMFUL }
        .randomOrNull()
        ?.let { effect ->
            val duration = effect.duration
            val maxTime = getEnhancedValue(user, SkillEnhancements.TIME_UP_LIMIT, 15 * 20.0)
            effect.setDuration(effect.mapDuration { (it - min(it * 0.2, maxTime)).toInt() })
            user.sendPacket(EntityStatusEffectS2CPacket(user.id, effect))
            val amount = (duration - effect.duration) / 20.0
            user.spawnParticles(
                ParticleTypes.GLOW,
                false, user.centerPos, (amount.toInt() * 10).coerceAtLeast(1),
                0.5, 0.5, 0.5, 0.1
            )
            return UseResult.success(
                message(
                    "success",
                    Text.translatable(effect.translationKey),
                    amount
                )
            )
        } ?: UseResult.fail(failedMessage())
}