package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import net.minecraft.entity.player.*
import net.minecraft.item.*
import net.minecraft.util.*

class FasterEatingSkill : PassiveSkill(
    id = "faster_eating",
    rarity = SkillRarity.SUPERB,
    enhancements = setOf(SkillEnhancements.EFFECT_VALUE)
), ItemMaxUseTimeTrigger {

    override fun getItemMaxUseTimeMultiplier(player: PlayerEntity, stack: ItemStack): Float {
        val effectValue = player.getEnhancementLvl(SkillEnhancements.EFFECT_VALUE) * 0.05f
        val useAction = stack.item.getUseAction(stack)
        return if (stack.isFood || stack.item is PotionItem || useAction == UseAction.EAT || useAction == UseAction.DRINK) -0.5f - effectValue else 0f
    }
}
