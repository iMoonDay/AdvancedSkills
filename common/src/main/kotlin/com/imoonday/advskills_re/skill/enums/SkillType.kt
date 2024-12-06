package com.imoonday.advskills_re.skill.enums

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.text.*

enum class SkillType(val representsSkill: () -> Skill) {
    ATTACK({ Skills.THUNDER_FURY }),
    DEFENSE({ Skills.ABSOLUTE_DEFENSE }),
    UTILITY({ Skills.ITEM_ATTRACTION }),
    CONTROL({ Skills.PRIMARY_FREEZE }),
    PASSIVE({ Skills.MASTERY }),
    ENHANCEMENT({ Skills.DOPING }),
    SUMMON({ Skills.EXCLUSIVE_MOUNT }),
    RESTORATION({ Skills.ADVANCED_PURIFICATION }),
    MOVEMENT({ Skills.DASH }),
    DESTRUCTION({ Skills.FIREBALL });

    val displayName: Text = translate("skillType." + name.lowercase())
}