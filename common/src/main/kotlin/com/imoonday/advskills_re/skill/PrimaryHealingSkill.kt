package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.util.SkillType

class PrimaryHealingSkill : HealingSkill(
    id = "primary_healing",
    types = listOf(SkillType.RESTORATION),
    cooldown = 15,
    rarity = Rarity.RARE,
    amount = 4.0f,
)