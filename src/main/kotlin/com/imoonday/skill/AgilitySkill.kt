package com.imoonday.skill

import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import java.util.*

class AgilitySkill : PassiveSkill(
    id = "agility",
    rarity = Rarity.RARE,
) {

    override fun getAttributes(): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            MOVEMENT_SPEED_UUID,
            "Agility",
            0.2,
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    companion object {
        @JvmField
        val MOVEMENT_SPEED_UUID: UUID = UUID.fromString("9A35B1AB-4F8F-41C6-8377-B369F8E39A61")
    }
}