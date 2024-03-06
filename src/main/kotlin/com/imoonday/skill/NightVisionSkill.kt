package com.imoonday.skill

import com.imoonday.trigger.PersistentTrigger
import com.imoonday.util.SkillType
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier

class NightVisionSkill : PassiveSkill(
    id = "night_vision",
    types = arrayOf(SkillType.PASSIVE),
    rarity = Rarity.SUPERB,
    toggleable = true
), PersistentTrigger {
    override val attribute: Map<EntityAttribute, EntityAttributeModifier> = emptyMap()
}