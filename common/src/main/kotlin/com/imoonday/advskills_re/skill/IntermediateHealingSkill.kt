package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.util.SkillType

class IntermediateHealingSkill : HealingSkill(
    id = "intermediate_healing",
    types = listOf(SkillType.RESTORATION),
    cooldown = 30,
    rarity = Rarity.SUPERB,
    amount = 8.0f,
)