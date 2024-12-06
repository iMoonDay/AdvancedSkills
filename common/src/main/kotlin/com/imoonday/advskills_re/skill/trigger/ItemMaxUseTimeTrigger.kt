package com.imoonday.advskills_re.skill.trigger

import net.minecraft.entity.player.*
import net.minecraft.item.*

interface ItemMaxUseTimeTrigger : SkillTrigger {

    fun getItemMaxUseTimeMultiplier(player: PlayerEntity, stack: ItemStack): Float
}