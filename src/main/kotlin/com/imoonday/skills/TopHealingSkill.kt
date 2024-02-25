package com.imoonday.skills

import com.imoonday.utils.SkillType

class TopHealingSkill : HealingSkill(
    id = "top_healing",
    types = arrayOf(SkillType.HEALING),
    cooldown = 60,
    rarity = Rarity.LEGENDARY,
    amount = 16.0f,
)
