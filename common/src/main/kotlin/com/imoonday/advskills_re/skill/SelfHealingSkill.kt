package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class SelfHealingSkill : Skill(
    id = "self_healing",
    types = listOf(SkillType.PASSIVE, SkillType.RESTORATION),
    rarity = SkillRarity.RARE,
    enhancements = setOf(SkillEnhancements.CHARGE_TIME, SkillEnhancements.HEALING_AMOUNT)
), AutoTrigger, AutoStopTrigger, DamageTrigger {

    override val timeParameterName: String = "charge_time"

    init {
        addEnhanceableParameter(timeParameterName, 10 * 20, "time", -0.16f, Enhancement.Type.MULTIPLY, 5) { (it * 100).toInt() }
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)

    override fun shouldStart(player: ServerPlayerEntity): Boolean = !player.isDead && player.health < player.maxHealth

    override fun onStop(player: ServerPlayerEntity) {
        val amount = getEnhancedValue(player, SkillEnhancements.HEALING_AMOUNT, 2.0f)
        player.heal(amount)
        player.spawnParticles(
            ParticleTypes.HEART,
            false, player.centerPos, amount.toInt(),
            0.5, 0.5, 0.5, 0.1
        )
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