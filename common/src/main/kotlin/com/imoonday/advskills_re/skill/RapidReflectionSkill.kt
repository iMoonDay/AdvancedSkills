package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.skill.enums.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

class RapidReflectionSkill : ReflectionSkill(
    id = "rapid_reflection",
    cooldown = 4,
    rarity = SkillRarity.RARE,
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