package com.imoonday.trigger

import com.imoonday.skill.*

interface SynchronousCoolingTrigger : SkillTrigger {

    fun getOtherSkills(): Set<Skill> = emptySet()
}