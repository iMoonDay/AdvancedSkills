package com.imoonday.skills

import com.imoonday.components.isUsingSkill
import com.imoonday.entities.Servant
import com.imoonday.triggers.AutoStopTrigger
import com.imoonday.triggers.PlayerDamageTrigger
import com.imoonday.utils.Skill
import com.imoonday.utils.SkillType
import com.imoonday.utils.UseResult
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity

class TauntSkill : Skill(
    id = "taunt",
    types = arrayOf(SkillType.ENHANCEMENT),
    cooldown = 30,
    rarity = Rarity.VERY_RARE,
), PlayerDamageTrigger, AutoStopTrigger {

    override val persistTime: Int = 20 * 15
    override val skill: Skill
        get() = this

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)
    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float = if (!player.isUsingSkill(this) || attacker !is Servant) amount else amount * 0.75f
}