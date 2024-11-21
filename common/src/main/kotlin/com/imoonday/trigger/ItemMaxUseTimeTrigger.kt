package com.imoonday.trigger

import net.minecraft.entity.player.*
import net.minecraft.item.*

interface ItemMaxUseTimeTrigger : SkillTrigger {

    fun getItemMaxUseTimeMultiplier(player: PlayerEntity, stack: ItemStack): Float
}