package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.skill.enums.*

class IntermediateHealingSkill : HealingSkill(
    id = "intermediate_healing",
    types = listOf(SkillType.RESTORATION),
    cooldown = 30,
    rarity = SkillRarity.SUPERB,
    amount = 8.0f,
)