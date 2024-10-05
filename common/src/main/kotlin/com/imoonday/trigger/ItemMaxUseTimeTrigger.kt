package com.imoonday.trigger

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack

interface ItemMaxUseTimeTrigger : SkillTrigger {

    fun getItemMaxUseTimeMultiplier(player: PlayerEntity, stack: ItemStack): Float
}