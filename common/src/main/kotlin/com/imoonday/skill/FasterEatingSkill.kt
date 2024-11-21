package com.imoonday.skill

import com.imoonday.trigger.*
import net.minecraft.entity.player.*
import net.minecraft.item.*
import net.minecraft.util.*

class FasterEatingSkill : PassiveSkill(
    id = "faster_eating",
    rarity = Rarity.SUPERB,
), ItemMaxUseTimeTrigger {

    override fun getItemMaxUseTimeMultiplier(player: PlayerEntity, stack: ItemStack): Float {
        val useAction = stack.item.getUseAction(stack)
        return if (stack.isFood || stack.item is PotionItem || useAction == UseAction.EAT || useAction == UseAction.DRINK) -0.5f else 0f
    }
}
