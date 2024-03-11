package com.imoonday.skill

import com.imoonday.trigger.ItemMaxUseTimeTrigger
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.PotionItem
import net.minecraft.util.UseAction

class FasterEatingSkill : PassiveSkill(
    id = "faster_eating",
    rarity = Rarity.SUPERB,
), ItemMaxUseTimeTrigger {

    override fun getItemMaxUseTimeMultiplier(player: PlayerEntity, stack: ItemStack): Float {
        val useAction = stack.item.getUseAction(stack)
        return if (stack.isFood || stack.item is PotionItem || useAction == UseAction.EAT || useAction == UseAction.DRINK) -0.5f else 0f
    }
}
