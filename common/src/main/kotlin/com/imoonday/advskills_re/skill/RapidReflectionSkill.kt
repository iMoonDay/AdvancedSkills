package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

class RapidReflectionSkill : ReflectionSkill(
    Settings(
        id = "rapid_reflection",
        cooldown = 4,
        rarity = SkillRarity.RARE
    ),
    duration = 10,
    baseChance = 0.5f
) {

    init {
        addParameter(
            name = "damage_reduction",
            baseValue = 0.5f,
            enhancementId = "multiplier",
            value = 0.05f,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )
    }

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float {
        if (!player.isUsing()) return amount
        player.stopUsing()
        player.modifyCooldown { it / 2 }
        player.reflect(attacker, amount)
        return amount * (1f - getFloatParam("damage_reduction", player, 0.5f, max = 1f))
    }
}