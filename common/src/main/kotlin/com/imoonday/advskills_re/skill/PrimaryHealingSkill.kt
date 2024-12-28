package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*

class PrimaryHealingSkill : HealingSkill(
    Settings(
        id = "primary_healing",
        types = listOf(SkillType.RESTORATION),
        cooldown = 15,
        rarity = SkillRarity.RARE
    ),
    4.0f,
)