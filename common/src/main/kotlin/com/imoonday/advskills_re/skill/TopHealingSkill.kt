package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.skill.enums.*

class TopHealingSkill : HealingSkill(
    id = "top_healing",
    types = listOf(SkillType.RESTORATION),
    cooldown = 60,
    rarity = SkillRarity.LEGENDARY,
    amount = 16.0f,
)
