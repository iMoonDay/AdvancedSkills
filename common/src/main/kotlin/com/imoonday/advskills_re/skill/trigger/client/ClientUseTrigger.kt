package com.imoonday.advskills_re.skill.trigger.client

import com.imoonday.advskills_re.skill.trigger.*
import net.minecraft.entity.player.*

interface ClientUseTrigger : SkillTrigger {

    fun onUse(clientPlayer: PlayerEntity) = Unit

    fun onStop(clientPlayer: PlayerEntity) = Unit
}