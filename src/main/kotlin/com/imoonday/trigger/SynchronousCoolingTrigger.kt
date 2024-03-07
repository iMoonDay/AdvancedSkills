package com.imoonday.trigger

import com.imoonday.skill.Skill

interface SynchronousCoolingTrigger : SkillTrigger {

    fun getOtherSkills(): Set<Skill> = emptySet()
}