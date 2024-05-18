package com.imoonday.skill

import com.imoonday.util.SkillType

class IntermediateHealingSkill : HealingSkill(
    id = "intermediate_healing",
    types = listOf(SkillType.RESTORATION),
    cooldown = 60,
    rarity = Rarity.SUPERB,
    amount = 8.0f,
)