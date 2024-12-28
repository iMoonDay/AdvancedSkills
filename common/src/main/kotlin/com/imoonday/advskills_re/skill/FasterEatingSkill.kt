package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.trigger.*
import net.minecraft.entity.player.*
import net.minecraft.item.*
import net.minecraft.util.*

class FasterEatingSkill : PassiveSkill(
    Settings(
        id = "faster_eating",
        rarity = SkillRarity.SUPERB
    ), customToggles = true
//    enhancements = setOf(SkillEnhancements.EFFECT_VALUE)
), ItemMaxUseTimeTrigger {

    override fun getItemMaxUseTimeMultiplier(player: PlayerEntity, stack: ItemStack): Float {
        if (!player.isAvailable()) return 0f

        val effectValue = player.getEnhancementLvl(SkillEnhancements.EFFECT_VALUE) * 0.05f
        val useAction = stack.item.getUseAction(stack)
        return if (stack.isFood
            || stack.item is PotionItem
            || useAction == UseAction.EAT
            || useAction == UseAction.DRINK
        ) -(0.5f + effectValue).coerceAtMost(1f) else 0f
    }
}
