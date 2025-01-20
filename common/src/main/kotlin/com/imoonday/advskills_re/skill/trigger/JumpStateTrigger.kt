package com.imoonday.advskills_re.skill.trigger

import net.minecraft.entity.player.*

interface JumpStateTrigger : SkillTrigger {

    fun shouldSyncJumpState(player: PlayerEntity): Boolean = true

    fun onJumped(player: PlayerEntity, onGround: Boolean) = Unit
}