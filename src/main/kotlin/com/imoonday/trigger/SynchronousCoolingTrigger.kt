package com.imoonday.trigger

import com.imoonday.skill.Skill

interface SynchronousCoolingTrigger {

    val otherSkills: Set<Skill>
}