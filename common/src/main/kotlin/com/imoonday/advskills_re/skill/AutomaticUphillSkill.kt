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
    ), true
), StepHeightTrigger, PersistentTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings.addParameter(
            name = "step_height",
            baseValue = 1.0f,
            enhancementId = "height",
            value = 0.5f,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.FLOAT
        )
        super.initDefaultSettings(settings)
    }

    override fun getStepHeight(player: PlayerEntity): Float? {
        if (!player.isAvailable()) return null
        return getFloatParam("step_height", player, 1.0f)
    }
}