package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class FoodlessSkill : PassiveSkill(
    Settings(
        id = "foodless",
        types = listOf(SkillType.PASSIVE, SkillType.UTILITY),
        rarity = SkillRarity.RARE
    )
), HungerTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings.addParameter(
            name = PARAM_MIN_HUNGER,
            baseValue = DEFAULT_MIN_HUNGER,
            enhancementId = ENHANCEMENT_MIN_HUNGER,
            value = 2,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT
        )
        super.initDefaultSettings(settings)
    }

    override fun onFoodLevelChange(player: PlayerEntity, level: Int): Int {
        if (!isAvailable(player)) return level

        val minHunger = getIntParam(PARAM_MIN_HUNGER, player, DEFAULT_MIN_HUNGER, 0)
        return level.coerceAtLeast(minHunger)
    }

    override fun onActivated(user: ServerPlayerEntity) {
        super.onActivated(user)
        val minHunger = getIntParam(PARAM_MIN_HUNGER, user, DEFAULT_MIN_HUNGER, 0)
        val hungerManager = user.hungerManager
        if (hungerManager.foodLevel < minHunger) {
            hungerManager.foodLevel = minHunger
        }
    }

    override fun isCustomToggles(): Boolean = true

    companion object {

        // Default Values
        private const val DEFAULT_MIN_HUNGER = 2

        // Parameter Names
        private const val PARAM_MIN_HUNGER = "min_hunger"  // 最低饥饿值

        // Enhancement IDs
        private const val ENHANCEMENT_MIN_HUNGER = "min_hunger"  // 对应最低饥饿值
    }
} 