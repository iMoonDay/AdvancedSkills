package com.imoonday.skill

import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

class RapidReflectionSkill : ReflectionSkill(
    id = "rapid_reflection",
    cooldown = 4,
    rarity = Rarity.RARE,
    duration = 10,
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
        if (player.random.nextBoolean()) {
            reflect(player, attacker, amount)
        } else {
            reflectedFailed(player)
        }
        return amount / 2
    }
}