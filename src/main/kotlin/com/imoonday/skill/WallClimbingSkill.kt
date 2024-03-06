package com.imoonday.skill

import com.imoonday.component.isUsingSkill
import com.imoonday.trigger.ClimbingTrigger
import com.imoonday.trigger.PersistentTrigger
import com.imoonday.util.SkillType
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.player.PlayerEntity

class WallClimbingSkill : PassiveSkill(
    id = "wall_climbing",
    types = arrayOf(SkillType.PASSIVE, SkillType.MOVEMENT),
    rarity = Rarity.RARE,
    toggleable = true
), ClimbingTrigger, PersistentTrigger {

    override val attribute: Map<EntityAttribute, EntityAttributeModifier> = emptyMap()

    override fun isClimbing(player: PlayerEntity): Boolean =
        if (!player.isUsingSkill(this)) false else player.horizontalCollision
}