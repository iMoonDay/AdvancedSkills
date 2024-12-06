package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.skill.enums.*

class AdvancedHealingSkill : HealingSkill(
    id = "advanced_healing",
    types = listOf(SkillType.RESTORATION),
    cooldown = 45,
    rarity = SkillRarity.EPIC,
    amount = 12.0f,
)