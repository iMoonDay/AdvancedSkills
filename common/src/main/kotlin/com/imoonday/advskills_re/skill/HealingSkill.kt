package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

abstract class HealingSkill(settings: Settings, amount: Float) : Skill(settings), SynchronousCoolingTrigger {

    init {
        addEnhanceableParameter(
            name = "healing_amount",
            baseValue = amount,
            enhancementId = "amount",
            value = 0.2f,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )
    }

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

    fun getHealingAmount(player: ServerPlayerEntity): Float = getFloatParam("healing_amount", player, 0f, 0f)

    override fun getOtherSkills(player: PlayerEntity): Set<Skill> =
        player.learnedSkills.filter { it is HealingSkill && it != this }.toSet()
}