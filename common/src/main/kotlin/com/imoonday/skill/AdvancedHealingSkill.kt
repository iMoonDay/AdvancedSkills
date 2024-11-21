package com.imoonday.skill

import com.imoonday.util.SkillType

class AdvancedHealingSkill : HealingSkill(
    id = "advanced_healing",
    types = listOf(SkillType.RESTORATION),
    cooldown = 45,
    rarity = Rarity.EPIC,
    amount = 12.0f,
)