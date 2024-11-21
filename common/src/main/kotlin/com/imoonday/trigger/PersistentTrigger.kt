package com.imoonday.trigger

import net.minecraft.entity.player.*

interface PersistentTrigger : UsingProgressTrigger {

    override fun getProgress(player: PlayerEntity): Double = 1.0
}