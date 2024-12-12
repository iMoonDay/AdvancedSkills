package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

class RapidReflectionSkill : ReflectionSkill(
    id = "rapid_reflection",
    cooldown = 4,
    rarity = SkillRarity.RARE,
    duration = 10,
    enhancements = setOf(SkillEnhancements.CHANCE, SkillEnhancements.DEFENSE_EFFECT)
) {

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float {
        if (!player.isUsing()) return amount
        player.stopUsing()
        player.modifyCooldown { it / 2 }
        player.reflect(0.5f, attacker, amount)
        return amount * (0.5f - player.getEnhancementLvl(SkillEnhancements.DEFENSE_EFFECT) * 0.05f).coerceAtLeast(0f)
    }
}