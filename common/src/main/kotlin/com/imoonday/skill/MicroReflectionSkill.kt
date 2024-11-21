package com.imoonday.skill

import com.imoonday.trigger.*
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*
import kotlin.random.*

class MicroReflectionSkill : Skill(
    id = "micro_reflection",
    types = listOf(SkillType.DEFENSE),
    cooldown = 6,
    rarity = Rarity.RARE
), DamageTrigger, ReflectionTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = startReflecting(user)

    override fun getPersistTime(): Int = 20

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float {
        if (!player.isUsing()) return amount
        player.stopUsing()
        if (Random.nextFloat() <= 0.25f) {
            reflect(player, attacker, amount / 2)
        } else {
            reflectedFailed(player)
        }
        return amount * 0.75f
    }
}