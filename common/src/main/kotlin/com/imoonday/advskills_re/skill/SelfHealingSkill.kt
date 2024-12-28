package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class SelfHealingSkill : Skill(
    id = "self_healing",
    types = listOf(SkillType.PASSIVE, SkillType.RESTORATION),
    rarity = SkillRarity.RARE,
    enhancements = setOf(SkillEnhancements.CHARGE_TIME, SkillEnhancements.HEALING_AMOUNT)
), AutoTrigger, AutoStopTrigger, DamageTrigger {

    override val timeParamName: String = "charge_time"

    init {
        addEnhanceableParameter(
            name = timeParamName,
            baseValue = 10 * 20,
            enhancementId = "time",
            value = -0.16f,
            operation = Enhancement.Operation.MULTIPLY,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)

    override fun shouldStart(player: ServerPlayerEntity): Boolean = !player.isDead && player.health < player.maxHealth

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        if (!player.hasEquipped()) return

        val amount = getEnhancedValue(player, SkillEnhancements.HEALING_AMOUNT, 2.0f)
        player.heal(amount)
        player.spawnParticles(
            ParticleTypes.HEART,
            false, player.centerPos, amount.toInt(),
            0.5, 0.5, 0.5, 0.1
        )
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

    override fun shouldFlashIcon(player: PlayerEntity): Boolean = false

    override fun getProgress(player: PlayerEntity): Double = 1.0 - super.getProgress(player)
}