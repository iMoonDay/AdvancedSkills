package com.imoonday.skill

import com.imoonday.trigger.*
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

class SelfHealingSkill : Skill(
    id = "self_healing",
    types = listOf(SkillType.PASSIVE, SkillType.RESTORATION),
    rarity = Rarity.RARE
), AutoTrigger, AutoStopTrigger, DamageTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name.string)

    override val persistTime: Int = 20 * 10

    override fun shouldStart(player: ServerPlayerEntity): Boolean = !player.isDead && player.health < player.maxHealth

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
        if (!player.isUsing()) return amount
        player.stopUsing()
        if (shouldStart(player)) {
            player.startUsing()
        }
        return amount
    }
}