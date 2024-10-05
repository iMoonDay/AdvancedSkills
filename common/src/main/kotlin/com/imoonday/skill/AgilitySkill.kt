package com.imoonday.skill

import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes

class AgilitySkill : PassiveSkill(
    id = "agility",
    rarity = Rarity.RARE,
) {

    override fun getAttributes(): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Agility"),
            "Agility",
            0.2,
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )
}