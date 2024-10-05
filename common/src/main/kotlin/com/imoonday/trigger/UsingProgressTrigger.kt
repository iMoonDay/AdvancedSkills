package com.imoonday.trigger

import net.minecraft.entity.player.PlayerEntity

interface UsingProgressTrigger : ProgressTrigger {

    override fun shouldDisplay(player: PlayerEntity): Boolean = true
}