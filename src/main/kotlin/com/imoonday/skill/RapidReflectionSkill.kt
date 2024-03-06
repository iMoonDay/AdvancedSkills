package com.imoonday.skill

import com.imoonday.component.isUsingSkill
import com.imoonday.component.modifyCooldown
import com.imoonday.component.stopUsingSkill
import com.imoonday.trigger.DamageTrigger
import com.imoonday.trigger.ReflectionTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity
import kotlin.random.Random

class RapidReflectionSkill : Skill(
    id = "rapid_reflection",
    types = arrayOf(SkillType.DEFENSE),
    cooldown = 4,
    rarity = Rarity.RARE
), DamageTrigger, ReflectionTrigger {
    override fun use(user: ServerPlayerEntity): UseResult = startReflecting(user)

    override val persistTime: Int = 10
    override val skill: Skill
        get() = this

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float {
        if (!player.isUsingSkill(this)) return amount
        player.stopUsingSkill(skill)
        player.modifyCooldown(skill) { it / 2 }
        if (Random.nextBoolean()) {
            reflect(player, attacker, amount)
        } else {
            reflectedFailed(player)
        }
        return amount / 2
    }
}