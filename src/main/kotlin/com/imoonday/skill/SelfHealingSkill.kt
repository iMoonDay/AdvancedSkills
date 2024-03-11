package com.imoonday.skill

import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.AutoTrigger
import com.imoonday.trigger.DamageTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity

class SelfHealingSkill : Skill(
    id = "self_healing",
    types = arrayOf(SkillType.PASSIVE, SkillType.HEALING),
    rarity = Rarity.RARE
), AutoTrigger, AutoStopTrigger, DamageTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name.string)

    override fun getPersistTime(): Int = 20 * 10

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
        if (!player.isUsing()) return amount
        player.stopUsing()
        if (shouldStart(player)) {
            player.startUsing()
        }
        return amount
    }
}