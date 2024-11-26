package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.util.SkillType

class TopHealingSkill : HealingSkill(
    id = "top_healing",
    types = listOf(SkillType.RESTORATION),
    cooldown = 60,
    rarity = Rarity.LEGENDARY,
    amount = 16.0f,
)
