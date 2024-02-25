package com.imoonday.trigger

import net.minecraft.entity.player.PlayerEntity

interface PersistentTrigger {

    fun isActive(player: PlayerEntity): Boolean = true
}