package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import net.minecraft.entity.player.*

class AutomaticUphillSkill : PassiveSkill(
    Settings(
        id = "automatic_uphill",
        types = listOf(SkillType.MOVEMENT),
        rarity = SkillRarity.RARE
    ), toggleable = true
), StepHeightTrigger, PersistentTrigger {

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings.addParameter(
            name = PARAM_STEP_HEIGHT,
            baseValue = DEFAULT_STEP_HEIGHT,
            enhancementId = ENHANCEMENT_HEIGHT,
            value = 0.5,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.FLOAT
        )
    }

    override fun getStepHeight(player: PlayerEntity): Float? {
        if (!isAvailable(player)) return null
        return getFloatParam(PARAM_STEP_HEIGHT, player, DEFAULT_STEP_HEIGHT)
    }

    companion object {

        // Default Values
        private const val DEFAULT_STEP_HEIGHT = 1.0f

        // Parameter Names
        private const val PARAM_STEP_HEIGHT = "step_height"  // 跨越高度

        // Enhancement IDs
        private const val ENHANCEMENT_HEIGHT = "height"  // 对应跨越高度
    }
}