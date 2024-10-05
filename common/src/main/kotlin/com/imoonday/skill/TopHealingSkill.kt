package com.imoonday.skill

import com.imoonday.util.SkillType

class TopHealingSkill : HealingSkill(
    id = "top_healing",
    types = listOf(SkillType.RESTORATION),
    cooldown = 60,
    rarity = Rarity.LEGENDARY,
    amount = 16.0f,
)
