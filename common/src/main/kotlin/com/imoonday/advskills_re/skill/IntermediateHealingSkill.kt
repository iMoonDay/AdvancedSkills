package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*

class IntermediateHealingSkill : HealingSkill(
    Settings(
        id = "intermediate_healing",
        types = listOf(SkillType.RESTORATION),
        cooldown = 30,
        rarity = SkillRarity.SUPERB
    )
) {

    override fun getDefaultHealingAmount(): Float = 8.0f
}