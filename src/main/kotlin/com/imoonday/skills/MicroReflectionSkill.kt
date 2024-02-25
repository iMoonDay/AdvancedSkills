package com.imoonday.skills

import com.imoonday.components.isUsingSkill
import com.imoonday.components.stopUsingSkill
import com.imoonday.trigger.PlayerDamageTrigger
import com.imoonday.trigger.ReflectionTrigger
import com.imoonday.utils.Skill
import com.imoonday.utils.SkillType
import com.imoonday.utils.UseResult
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity
import kotlin.random.Random

class MicroReflectionSkill : Skill(
    id = "micro_reflection",
    types = arrayOf(SkillType.DEFENSE),
    cooldown = 6,
    rarity = Rarity.RARE
), PlayerDamageTrigger, ReflectionTrigger {
    override fun use(user: ServerPlayerEntity): UseResult = startReflecting(user)

    override val persistTime: Int = 20
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
        if (Random.nextFloat() <= 0.25f) {
            reflect(player, attacker, amount / 2)
        } else {
            reflectedFailed(player)
        }
        return amount * 0.75f
    }
}