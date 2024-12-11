package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.player.*

class AgilitySkill : PassiveSkill(
    id = "agility",
    rarity = SkillRarity.RARE,
    enhancements = setOf(SkillEnhancements.MOVEMENT_SPEED),
) {

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Agility"),
            "Agility",
            0.2 + player.getEnhancementLvl(SkillEnhancements.MOVEMENT_SPEED) * 0.04,
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )
}