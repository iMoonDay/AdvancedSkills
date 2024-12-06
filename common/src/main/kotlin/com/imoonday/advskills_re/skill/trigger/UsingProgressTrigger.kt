package com.imoonday.advskills_re.skill.trigger

import net.minecraft.entity.player.*

interface UsingProgressTrigger : ProgressTrigger {

    override fun shouldDisplay(player: PlayerEntity): Boolean = true
}