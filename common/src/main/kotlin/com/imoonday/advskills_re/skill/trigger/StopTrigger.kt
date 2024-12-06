package com.imoonday.advskills_re.skill.trigger

import net.minecraft.entity.player.*

interface StopTrigger : SkillTrigger {

    fun postStop(player: PlayerEntity) = Unit
}