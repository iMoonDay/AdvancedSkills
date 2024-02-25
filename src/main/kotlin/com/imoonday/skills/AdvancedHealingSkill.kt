package com.imoonday.skills

import com.imoonday.utils.SkillType

class AdvancedHealingSkill : HealingSkill(
    id = "advanced_healing",
    types = arrayOf(SkillType.HEALING),
    cooldown = 60,
    rarity = Rarity.EPIC,
    amount = 12.0f,
)