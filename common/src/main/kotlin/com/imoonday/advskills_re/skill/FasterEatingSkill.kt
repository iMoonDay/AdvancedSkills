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

    override fun initDefaultSettings(settings: Settings) {
        settings.addParameter(
            name = "speed_multiplier",
            baseValue = 0.5f,
            enhancementId = "multiplier",
            value = 0.05f,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT
        )
        super.initDefaultSettings(settings)
    }

    override fun getItemMaxUseTimeMultiplier(player: PlayerEntity, stack: ItemStack): Float {
        if (!player.isAvailable()) return 0f

        val useAction = stack.item.getUseAction(stack)
        if (!(stack.isFood
                || stack.item is PotionItem
                || useAction == UseAction.EAT
                || useAction == UseAction.DRINK)
        ) return 0f

        return -getFloatParam("speed_multiplier", player, 0.5f, 0f, 1f)
    }
}
