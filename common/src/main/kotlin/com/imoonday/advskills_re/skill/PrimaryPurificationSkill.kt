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

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter("purify_sound", ModSounds.PURIFY)
            .addParameter(
                name = "max_duration",
                baseValue = 15 * 20,
                enhancementId = "time",
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
            val duration = effect.duration
            val maxTime = getIntParam("max_duration", user, 15 * 20).toDouble()
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
                ),
                getSoundEventParam("purify_sound", ModSounds.PURIFY.get())
            )
        } ?: UseResult.fail(failedMessage())
}