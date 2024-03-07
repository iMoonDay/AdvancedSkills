package com.imoonday.trigger

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack

interface BowSpeedTrigger {

    fun getSpeedMultiplier(player: PlayerEntity, stack: ItemStack): Float = 0f
}