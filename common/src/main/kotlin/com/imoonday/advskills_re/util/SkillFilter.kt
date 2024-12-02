package com.imoonday.advskills_re.util

import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.trigger.*
import net.minecraft.text.*

enum class SkillFilter : (Skill) -> Boolean {
    NONE {

        override fun invoke(skill: Skill): Boolean = true
    },
    NO_COOLING {

        override fun invoke(skill: Skill): Boolean = skill.cooldown <= 0
    },
    LONG_PRESS {

        override fun invoke(skill: Skill): Boolean = skill is LongPressTrigger
    },
    ACTIVE {

        override fun invoke(skill: Skill): Boolean = SkillType.PASSIVE !in skill.types
    };

    val displayName: Text = translate("skillFilter.${name.lowercase()}")

    fun next(): SkillFilter = entries[(ordinal + 1) % entries.size]

    fun previous(): SkillFilter = entries[(ordinal + entries.size - 1) % entries.size]
}