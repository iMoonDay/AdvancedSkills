package com.imoonday.advskills_re.skill.trigger.client

import com.imoonday.advskills_re.skill.trigger.*
import net.minecraft.entity.player.*

interface CameraUpdateMovementTrigger : SkillTrigger {

    fun getDelta(original: Float, clientPlayer: PlayerEntity): Float = original
}