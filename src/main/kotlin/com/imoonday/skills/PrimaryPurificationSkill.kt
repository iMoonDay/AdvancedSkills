package com.imoonday.skills

import com.imoonday.init.ModSounds
import com.imoonday.utils.*
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import kotlin.math.min

class PrimaryPurificationSkill : Skill(
    id = "primary_purification",
    types = arrayOf(SkillType.RESTORATION),
    cooldown = 30,
    rarity = Rarity.VERY_RARE,
    sound = ModSounds.PURIFY
) {
    override fun use(user: ServerPlayerEntity): UseResult = user.statusEffects
        .filter { it.effectType.category == StatusEffectCategory.HARMFUL }
        .randomOrNull()
        ?.let {
            val duration = it.duration
            it.setDuration(it.mapDuration { (it - min(it * 0.2, 15 * 20.0)).toInt() })
            user.send(EntityStatusEffectS2CPacket(user.id, it))
            val amount = (duration - it.duration) / 20.0
            return UseResult.success(
                Text.translatable(
                    "advancedSkills.skill.primary_purification.success",
                    Text.translatable(it.translationKey).string,
                    amount
                )
            )
        } ?: UseResult.fail(Text.translatable("advancedSkills.skill.primary_purification.failed"))
}