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

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(
                name = PARAM_CHARGE_DURATION,
                baseValue = DEFAULT_CHARGE_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = -0.16,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_HEAL_AMOUNT,
                baseValue = DEFAULT_HEAL_AMOUNT,
                enhancementId = ENHANCEMENT_AMOUNT,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT,
                genericText = true
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)

    override fun shouldStart(player: ServerPlayerEntity): Boolean = !player.isDead && player.health < player.maxHealth

    override fun getMaxUseTime(player: PlayerEntity): Int = 
        getIntParam(PARAM_CHARGE_DURATION, player, DEFAULT_CHARGE_DURATION, 0)

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        if (!player.hasEquipped()) return

        val amount = getFloatParam(PARAM_HEAL_AMOUNT, player, DEFAULT_HEAL_AMOUNT)
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

    companion object {
        // Default Values
        private const val DEFAULT_CHARGE_DURATION = 10 * 20
        private const val DEFAULT_HEAL_AMOUNT = 2.0f

        // Parameter Names
        private const val PARAM_CHARGE_DURATION = "charge_duration"  // 充能时间
        private const val PARAM_HEAL_AMOUNT = "heal_amount"  // 治疗量

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
        private const val ENHANCEMENT_AMOUNT = "heal_amount"  // 对应治疗量
    }
}