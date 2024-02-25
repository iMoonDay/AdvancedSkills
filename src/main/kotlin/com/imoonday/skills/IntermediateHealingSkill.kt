package com.imoonday.skills

import com.imoonday.utils.SkillType

class IntermediateHealingSkill: HealingSkill(
    id = "intermediate_healing",
    types = arrayOf(SkillType.HEALING),
    cooldown = 60,
    rarity = Rarity.VERY_RARE,
    amount = 8.0f,
)