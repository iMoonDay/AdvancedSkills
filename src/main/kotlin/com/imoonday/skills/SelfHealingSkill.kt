package com.imoonday.skills

import com.imoonday.components.isUsingSkill
import com.imoonday.components.startUsingSkill
import com.imoonday.components.stopUsingSkill
import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.AutoTrigger
import com.imoonday.trigger.PlayerDamageTrigger
import com.imoonday.utils.*
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity

class SelfHealingSkill : Skill(
    id = "self_healing",
    types = arrayOf(SkillType.PASSIVE, SkillType.HEALING),
    rarity = Rarity.RARE
), AutoTrigger, AutoStopTrigger, PlayerDamageTrigger {
    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name.string)

    override val persistTime: Int = 20 * 10

    override val skill: Skill
        get() = this

    override fun shouldStart(player: ServerPlayerEntity): Boolean = player.health < player.maxHealth

    override fun onStop(player: ServerPlayerEntity) {
        player.heal(2.0f)
        super.onStop(player)
    }

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float {
        if (!player.isUsingSkill(this)) return amount
        player.stopUsingSkill(skill)
        if (shouldStart(player)) {
            player.startUsingSkill(skill)
        }
        return amount
    }
}