package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class SelfHealingSkill : Skill(
    Settings(
        id = "self_healing",
        types = listOf(SkillType.PASSIVE, SkillType.RESTORATION),
        rarity = SkillRarity.RARE
    )
), AutoTrigger, AutoStopTrigger, PostDamagedTrigger {

    override val timeParamName: String = AutoStopTrigger.CHARGE_TIME

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(
                name = AutoStopTrigger.CHARGE_TIME,
                baseValue = 10 * 20,
                enhancementId = "time",
                value = -0.16,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "healing_amount",
                baseValue = 2.0f,
                enhancementId = "amount",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)

    override fun shouldStart(player: ServerPlayerEntity): Boolean = !player.isDead && player.health < player.maxHealth

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        if (!player.hasEquipped()) return

        val amount = getFloatParam("healing_amount", player, 2.0f)
        player.heal(amount)
        player.spawnParticles(
            ParticleTypes.HEART, false, player.centerPos, amount.toInt(), 0.5, 0.5, 0.5, 0.1
        )
    }

    override fun postDamaged(amount: Float, source: DamageSource, player: ServerPlayerEntity, attacker: LivingEntity?) {
        super.postDamaged(amount, source, player, attacker)
        if (!player.isUsing()) return
        player.stopUsing()
        if (shouldStart(player)) {
            player.startUsing()
        }
    }

    override fun shouldFlashIcon(player: PlayerEntity): Boolean = false

    override fun getProgress(player: PlayerEntity): Double = 1.0 - super.getProgress(player)
}