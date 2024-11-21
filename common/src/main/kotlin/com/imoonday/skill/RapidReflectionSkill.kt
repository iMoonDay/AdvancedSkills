package com.imoonday.skill

import com.imoonday.trigger.*
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*
import kotlin.random.*

class RapidReflectionSkill : Skill(
    id = "rapid_reflection",
    types = listOf(SkillType.DEFENSE),
    cooldown = 4,
    rarity = Rarity.RARE
), DamageTrigger, ReflectionTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = startReflecting(user)

    override fun getPersistTime(): Int = 10

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float {
        if (!player.isUsing()) return amount
        player.stopUsing()
        player.modifyCooldown { it / 2 }
        if (Random.nextBoolean()) {
            reflect(player, attacker, amount)
        } else {
            reflectedFailed(player)
        }
        return amount / 2
    }
}