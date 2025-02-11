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
    healingAmount = 6.0f
) {

    override fun cooldownWithOthers(skill: Skill): Boolean = false

    override fun getDefaultEnhancementValue(): Double = 2.0
    override fun getDefaultEnhancementDescArg(): Enhancement.ArgFormatter = Enhancement.ArgFormatter.INT
    override fun getDefaultEnhancementLevel(): Int = 7
    override fun getDefaultEnhancementOperation(): Enhancement.Operation = Enhancement.Operation.ADDITION
}