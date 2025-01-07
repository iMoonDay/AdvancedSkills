package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.player.*

class AgilitySkill : PassiveSkill(
    Settings(
        id = "agility",
        rarity = SkillRarity.RARE
    ), customToggles = true
) {

    init {
        settings.addParameter(
            name = PARAM_SPEED_BOOST,
            baseValue = DEFAULT_SPEED_BOOST,
            enhancementId = ENHANCEMENT_SPEED,
            value = 0.04,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT
        )
    }

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Agility"),
            "Agility",
            getDoubleParam(PARAM_SPEED_BOOST, player, DEFAULT_SPEED_BOOST, 0.0),
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    companion object {

        // Default Values
        private const val DEFAULT_SPEED_BOOST = 0.2

        // Parameter Names
        private const val PARAM_SPEED_BOOST = "movement_speed_boost"  // 移动速度提升

        // Enhancement IDs
        private const val ENHANCEMENT_SPEED = "speed"  // 对应速度提升
    }
}