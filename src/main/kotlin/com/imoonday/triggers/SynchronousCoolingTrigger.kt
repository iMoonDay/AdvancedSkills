package com.imoonday.triggers

import com.imoonday.utils.Skill

interface SynchronousCoolingTrigger {

    val otherSkills: Set<Skill>
}