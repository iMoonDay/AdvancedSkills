package com.imoonday.trigger

import net.minecraft.entity.player.PlayerEntity

interface PersistentTrigger : UsingProgressTrigger {
    override fun getProgress(player: PlayerEntity): Double = 1.0
}