package com.imoonday.skill

import com.imoonday.util.SkillType

class PrimaryHealingSkill : HealingSkill(
    id = "primary_healing",
    types = arrayOf(SkillType.HEALING),
    cooldown = 60,
    rarity = Rarity.RARE,
    amount = 4.0f,
)