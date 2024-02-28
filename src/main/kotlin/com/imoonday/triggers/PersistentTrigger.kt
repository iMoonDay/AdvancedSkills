package com.imoonday.triggers

import net.minecraft.entity.player.PlayerEntity

interface PersistentTrigger {

    fun isActive(player: PlayerEntity): Boolean = true
}