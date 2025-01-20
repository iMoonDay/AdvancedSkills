package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

abstract class HealingSkill(
    settings: Settings,
    private val healingAmount: Float,
) : Skill(settings), SynchronousCoolingTrigger {

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings
            .addParameter(PARAM_HEAL_SOUND, DEFAULT_HEAL_SOUND)
            .addParameter(
                name = PARAM_HEAL_AMOUNT,
                baseValue = healingAmount,
                enhancementId = ENHANCEMENT_AMOUNT,
                value = getDefaultEnhancementValue(),
                operation = getDefaultEnhancementOperation(),
                maxLevel = getDefaultEnhancementLevel(),
                descArg = getDefaultEnhancementDescArg(),
                genericText = true
            )
    }

    protected open fun getDefaultEnhancementValue(): Number = 0.2f
    protected open fun getDefaultEnhancementOperation(): Enhancement.Operation = Enhancement.Operation.MULTIPLY_TOTAL
    protected open fun getDefaultEnhancementLevel(): Int = 5
    protected open fun getDefaultEnhancementDescArg(): Enhancement.ArgFormatter = Enhancement.ArgFormatter.INT_PERCENT

    override fun use(user: ServerPlayerEntity): UseResult {
        val healingAmount = getHealingAmount(user)
        user.heal(healingAmount)
        user.spawnParticles(
            ParticleTypes.HEART,
            false, user.centerPos, healingAmount.toInt(),
            0.5, 0.5, 0.5, 0.1
        )
        return UseResult.success(sound = getSoundEventParam(PARAM_HEAL_SOUND, DEFAULT_HEAL_SOUND.get()))
    }

    fun getHealingAmount(player: ServerPlayerEntity): Float =
        getFloatParam(PARAM_HEAL_AMOUNT, player, healingAmount, 0f)

    override fun getOtherSkills(player: PlayerEntity): Set<Skill> =
        player.learnedSkills.filter {
            it is HealingSkill
                && it != this
                && it.cooldownWithOthers(this)
                && cooldownWithOthers(it)
        }.toSet()

    open fun cooldownWithOthers(skill: Skill): Boolean = true

    companion object {

        // Default Values
        private val DEFAULT_HEAL_SOUND = ModSounds.HEAL

        // Parameter Names
        private const val PARAM_HEAL_AMOUNT = "heal_amount"  // 治疗量
        private const val PARAM_HEAL_SOUND = "heal_sound"  // 治疗音效

        // Enhancement IDs
        private const val ENHANCEMENT_AMOUNT = "heal_amount"  // 对应治疗量
    }
}