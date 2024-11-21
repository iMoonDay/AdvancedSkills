package com.imoonday.skill

import com.imoonday.init.*
import com.imoonday.util.*
import net.minecraft.entity.effect.*
import net.minecraft.network.packet.s2c.play.*
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
        .filter { it.effectType.category == StatusEffectCategory.HARMFUL && it.duration < 20 * 30 }
        .maxByOrNull { it.duration }
        ?.let {
            user.removeStatusEffect(it.effectType)
            return UseResult.success(
                translateSkill(
                    id.path, "success",
                    Text.translatable(it.translationKey).string
                )
            )
        } ?: user.statusEffects
        .filter { it.effectType.category == StatusEffectCategory.HARMFUL }
        .randomOrNull()
        ?.let {
            val duration = it.duration
            it.setDuration(it.mapDuration { it - 30 * 20 })
            user.send(EntityStatusEffectS2CPacket(user.id, it))
            val amount = (duration - it.duration) / 20.0
            return UseResult.success(
                translateSkill(
                    "primary_purification", "success",
                    Text.translatable(it.translationKey).string,
                    amount
                )
            )
        }
    ?: UseResult.fail(failedMessage())
}