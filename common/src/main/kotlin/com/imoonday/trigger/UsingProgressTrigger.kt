package com.imoonday.trigger

import net.minecraft.entity.player.*

interface UsingProgressTrigger : ProgressTrigger {

    override fun shouldDisplay(player: PlayerEntity): Boolean = true
}