package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

abstract class HealingSkill(settings: Settings) : Skill(settings),
    SynchronousCoolingTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings.addParameter(
            name = PARAM_HEAL_AMOUNT,
            baseValue = getDefaultHealingAmount(),
            enhancementId = ENHANCEMENT_AMOUNT,
            value = 0.2f,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT,
            genericText = true
        )
    }

    abstract fun getDefaultHealingAmount(): Float

    override fun use(user: ServerPlayerEntity): UseResult {
        val healingAmount = getHealingAmount(user)
        user.heal(healingAmount)
        user.spawnParticles(
            ParticleTypes.HEART,
            false, user.centerPos, healingAmount.toInt(),
            0.5, 0.5, 0.5, 0.1
        )
        return UseResult.success()
    }

    fun getHealingAmount(player: ServerPlayerEntity): Float =
        getFloatParam(PARAM_HEAL_AMOUNT, player, getDefaultHealingAmount(), 0f)

    override fun getOtherSkills(player: PlayerEntity): Set<Skill> =
        player.learnedSkills.filter { it is HealingSkill && it != this }.toSet()

    companion object {

        // Parameter Names
        private const val PARAM_HEAL_AMOUNT = "heal_amount"  // 治疗量

        // Enhancement IDs
        private const val ENHANCEMENT_AMOUNT = "heal_amount"  // 对应治疗量
    }
}