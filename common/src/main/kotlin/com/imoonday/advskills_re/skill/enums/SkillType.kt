package com.imoonday.advskills_re.skill.enums

import com.google.gson.annotations.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.text.*

enum class SkillType(val representsSkill: () -> Skill) {
    @SerializedName("attack")
    ATTACK({ Skills.THUNDER_FURY }),

    @SerializedName("defense")
    DEFENSE({ Skills.ABSOLUTE_DEFENSE }),

    @SerializedName("utility")
    UTILITY({ Skills.ITEM_ATTRACTION }),

    @SerializedName("control")
    CONTROL({ Skills.PRIMARY_FREEZE }),

    @SerializedName("passive")
    PASSIVE({ Skills.MASTERY }),

    @SerializedName("enhancement")
    ENHANCEMENT({ Skills.DOPING }),

    @SerializedName("summon")
    SUMMON({ Skills.EXCLUSIVE_MOUNT }),

    @SerializedName("restoration")
    RESTORATION({ Skills.ADVANCED_PURIFICATION }),

    @SerializedName("movement")
    MOVEMENT({ Skills.DASH }),

    @SerializedName("destruction")
    DESTRUCTION({ Skills.FIREBALL });

    val displayName: Text = translate("skillType." + name.lowercase())
}