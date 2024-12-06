package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.skill.enums.*
import net.minecraft.entity.attribute.*

class AgilitySkill : PassiveSkill(
    id = "agility",
    rarity = SkillRarity.RARE,
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