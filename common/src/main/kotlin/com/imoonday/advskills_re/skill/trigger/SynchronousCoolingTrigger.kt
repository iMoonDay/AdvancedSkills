package com.imoonday.advskills_re.skill.trigger

import com.imoonday.advskills_re.skill.*
import net.minecraft.entity.player.*

interface SynchronousCoolingTrigger : SkillTrigger {

    fun getOtherSkills(player: PlayerEntity): Set<Skill> = emptySet()
}