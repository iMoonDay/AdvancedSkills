package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*

class InstantHealingSkill : HealingSkill(
    Settings(
        id = "instant_healing",
        types = listOf(SkillType.RESTORATION),
        cooldown = 25,
        rarity = SkillRarity.EPIC
    ),
    healingAmount = 6.0f,
    enhancementValue = 2,
    enhancementOperation = Enhancement.Operation.ADDITION,
    enhancementLevel = 7,
    enhancementDescArg = Enhancement.ArgFormatter.INT
) {

    override fun cooldownWithOthers(skill: Skill): Boolean = false
}