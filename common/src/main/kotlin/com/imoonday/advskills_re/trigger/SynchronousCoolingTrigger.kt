package com.imoonday.advskills_re.trigger

import com.imoonday.advskills_re.skill.*

interface SynchronousCoolingTrigger : SkillTrigger {

    fun getOtherSkills(): Set<Skill> = emptySet()
}