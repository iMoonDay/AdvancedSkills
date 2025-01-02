package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

class MicroReflectionSkill : ReflectionSkill(
    Settings(
        id = "micro_reflection",
        cooldown = 6,
        rarity = SkillRarity.RARE
    ),
    duration = 20,
    baseChance = 0.25f
) {

    init {
        addParameter(
            name = "damage_reduction",
            baseValue = 0.25f,
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
        player.reflect(attacker, amount / 2)
        return amount * (1f - getFloatParam("damage_reduction", player, 0.25f, max = 1f))
    }
}