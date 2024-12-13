package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*

class SelfHealingSkill : Skill(
    id = "self_healing",
    types = listOf(SkillType.PASSIVE, SkillType.RESTORATION),
    rarity = SkillRarity.RARE,
    enhancements = setOf(SkillEnhancements.CHARGE_TIME, SkillEnhancements.HEALING_AMOUNT)
), AutoTrigger, AutoStopTrigger, DamageTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)

    override val persistTime: Int = 10 * 20

    override fun shouldStart(player: ServerPlayerEntity): Boolean = !player.isDead && player.health < player.maxHealth

    override fun onStop(player: ServerPlayerEntity) {
        val amount = getEnhancedValue(player, SkillEnhancements.HEALING_AMOUNT, 2.0f)
        player.heal(amount)
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