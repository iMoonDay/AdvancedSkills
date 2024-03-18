package com.imoonday.util

import com.imoonday.skill.Skill

data class SkillContainer(
    val learned: MutableMap<Skill, SkillData>,
    val equipped: MutableMap<SkillSlot, Skill>,
) {
}