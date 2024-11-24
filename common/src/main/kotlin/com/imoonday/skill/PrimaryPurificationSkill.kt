package com.imoonday.skill

import com.imoonday.init.*
import com.imoonday.util.*
import net.minecraft.entity.effect.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.server.network.*
import net.minecraft.text.*
import kotlin.math.*

class PrimaryPurificationSkill : Skill(
    id = "primary_purification",
    types = listOf(SkillType.RESTORATION),
    cooldown = 30,
    rarity = Rarity.SUPERB,
    sound = ModSounds.PURIFY
) {

    override fun use(user: ServerPlayerEntity): UseResult = user.statusEffects
        .filter { it.effectType.category == StatusEffectCategory.HARMFUL }
        .randomOrNull()
        ?.let {
            val duration = it.duration
            it.setDuration(it.mapDuration { (it - min(it * 0.2, 15 * 20.0)).toInt() })
            user.sendToServer(EntityStatusEffectS2CPacket(user.id, it))
            val amount = (duration - it.duration) / 20.0
            return UseResult.success(
                translateSkill(
                    id.path, "success",
                    Text.translatable(it.translationKey).string,
                    amount
                )
            )
        } ?: UseResult.fail(failedMessage())
}