package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*

class TopHealingSkill : HealingSkill(
    Settings(
        id = "top_healing",
        types = listOf(SkillType.RESTORATION),
        cooldown = 60,
        rarity = SkillRarity.LEGENDARY
    ),
    16.0f,
)
