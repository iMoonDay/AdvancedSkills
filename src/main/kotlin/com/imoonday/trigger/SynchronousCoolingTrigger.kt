package com.imoonday.trigger

import com.imoonday.utils.Skill

interface SynchronousCoolingTrigger {

    val otherSkills: Set<Skill>
}