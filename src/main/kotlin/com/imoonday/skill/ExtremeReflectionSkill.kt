package com.imoonday.skill

import com.imoonday.trigger.DamageTrigger
import com.imoonday.trigger.ReflectionTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity
import kotlin.random.Random

class ExtremeReflectionSkill : Skill(
    id = "extreme_reflection",
    types = arrayOf(SkillType.DEFENSE),
    cooldown = 3,
    rarity = Rarity.RARE
), DamageTrigger, ReflectionTrigger {
    override fun use(user: ServerPlayerEntity): UseResult = startReflecting(user)

    override fun getPersistTime(): Int = 5

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float {
        if (!player.isUsing()) return amount
        player.stopUsing()
        player.stopCooling()
        return if (Random.nextFloat() <= 0.75f) {
            reflect(player, attacker, amount)
            0.0f
        } else {
            reflectedFailed(player)
            amount
        }
    }
}