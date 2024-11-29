package com.imoonday.advskills_re.trigger

import net.minecraft.entity.player.*

interface CameraUpdateMovementTrigger : SkillTrigger {

    fun getDelta(original: Float, player: PlayerEntity): Float = original
}