package com.imoonday.advskills_re.skill

import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

class MicroReflectionSkill : ReflectionSkill(
    id = "micro_reflection",
    cooldown = 6,
    rarity = Rarity.RARE,
    duration = 20
) {

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float {
        if (!player.isUsing()) return amount
        player.stopUsing()
        if (player.random.nextFloat() <= 0.25f) {
            reflect(player, attacker, amount / 2)
        } else {
            reflectedFailed(player)
        }
        return amount * 0.75f
    }
}