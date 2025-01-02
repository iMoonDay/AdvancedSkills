package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

class ExtremeReflectionSkill : ReflectionSkill(
    Settings(
        id = "extreme_reflection",
        cooldown = 3,
        rarity = SkillRarity.RARE
    ),
    duration = 5,
    baseChance = 0.75f
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
        return player.reflect(attacker as? LivingEntity, amount)
    }
}