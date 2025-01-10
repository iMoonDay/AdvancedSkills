package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*

class AdvancedHealingSkill : HealingSkill(
    Settings(
        id = "advanced_healing",
        types = listOf(SkillType.RESTORATION),
        cooldown = 45,
        rarity = SkillRarity.EPIC
    ).withDisabled(true), 12.0f
)