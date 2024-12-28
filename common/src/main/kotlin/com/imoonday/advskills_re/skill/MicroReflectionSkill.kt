package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

class MicroReflectionSkill : ReflectionSkill(
    id = "micro_reflection",
    cooldown = 6,
    rarity = SkillRarity.RARE,
    duration = 20,
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
        player.reflect(0.25f, attacker, amount / 2)
        return amount * (0.75f - player.getEnhancementLvl(SkillEnhancements.DEFENSE_EFFECT) * 0.05f).coerceAtLeast(0f)
    }
}