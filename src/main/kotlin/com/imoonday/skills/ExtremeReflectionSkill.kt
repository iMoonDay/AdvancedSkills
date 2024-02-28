package com.imoonday.skills

import com.imoonday.components.isUsingSkill
import com.imoonday.components.stopCooling
import com.imoonday.components.stopUsingSkill
import com.imoonday.triggers.PlayerDamageTrigger
import com.imoonday.triggers.ReflectionTrigger
import com.imoonday.utils.Skill
import com.imoonday.utils.SkillType
import com.imoonday.utils.UseResult
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity
import kotlin.random.Random

class ExtremeReflectionSkill : Skill(
    id = "extreme_reflection",
    types = arrayOf(SkillType.DEFENSE),
    cooldown = 3,
    rarity = Rarity.RARE
), PlayerDamageTrigger, ReflectionTrigger {
    override fun use(user: ServerPlayerEntity): UseResult = startReflecting(user)

    override val persistTime: Int = 5
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
        player.stopCooling(skill)
        return if (Random.nextFloat() <= 0.75f) {
            reflect(player, attacker, amount)
            0.0f
        } else {
            reflectedFailed(player)
            amount
        }
    }
}