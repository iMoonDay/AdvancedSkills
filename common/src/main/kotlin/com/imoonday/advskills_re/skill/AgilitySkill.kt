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
        addParameter(
            name = "speed_multiplier",
            baseValue = 0.2,
            enhancementId = "speed",
            value = 0.04,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )
    }

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Agility"),
            "Agility",
            getDoubleParam("speed_multiplier", player, 0.2, 0.0),
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )
}