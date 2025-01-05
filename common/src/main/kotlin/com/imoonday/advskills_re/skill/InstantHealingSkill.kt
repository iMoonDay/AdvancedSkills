package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*

class InstantHealingSkill : HealingSkill(
    Settings(
        id = "instant_healing",
        types = listOf(SkillType.RESTORATION),
        cooldown = 25,
        rarity = SkillRarity.EPIC
    )
) {

    override fun getDefaultHealingAmount(): Float = 6.0f
}