package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.trigger.*
import net.minecraft.entity.player.*
import net.minecraft.item.*
import net.minecraft.util.*

class FasterEatingSkill : PassiveSkill(
    Settings(
        id = "faster_eating",
        rarity = SkillRarity.SUPERB
    ), customToggles = true
), ItemMaxUseTimeTrigger {

    init {
        settings.addParameter(
            name = PARAM_SPEED_BOOST,
            baseValue = DEFAULT_SPEED_BOOST,
            enhancementId = ENHANCEMENT_SPEED,
            value = 0.05f,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT
        )
    }

    override fun getItemMaxUseTimeMultiplier(player: PlayerEntity, stack: ItemStack): Float {
        if (!isAvailable(player)) return 0f

        val useAction = stack.item.getUseAction(stack)
        if (!(stack.isFood
                || stack.item is PotionItem
                || useAction == UseAction.EAT
                || useAction == UseAction.DRINK)
        ) return 0f

        return -getFloatParam(PARAM_SPEED_BOOST, player, DEFAULT_SPEED_BOOST, 0f, 1f)
    }

    companion object {

        // Default Values
        private const val DEFAULT_SPEED_BOOST = 0.5f

        // Parameter Names
        private const val PARAM_SPEED_BOOST = "consume_speed_boost"  // 消耗速度提升

        // Enhancement IDs
        private const val ENHANCEMENT_SPEED = "speed"  // 对应速度提升
    }
}
