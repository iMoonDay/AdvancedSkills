package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

class ExtremeReflectionSkill : ReflectionSkill(
    id = "extreme_reflection",
    cooldown = 3,
    rarity = SkillRarity.RARE,
    duration = 5,
    enhancements = setOf(SkillEnhancements.CHANCE)
) {

    override fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?,
    ): Boolean {
        if (!player.isUsing()) return false
        player.stopUsing()
        player.stopCooling()
        return player.reflect(0.75f, attacker as? LivingEntity, amount)
    }
}