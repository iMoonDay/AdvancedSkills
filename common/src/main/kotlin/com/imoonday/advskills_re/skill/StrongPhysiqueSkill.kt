package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.util.SkillSlot
import net.minecraft.entity.attribute.*
import net.minecraft.server.network.*

class StrongPhysiqueSkill : PassiveSkill(
    id = "strong_physique",
    rarity = Rarity.SUPERB
) {

    override fun getAttributes(): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MAX_HEALTH to EntityAttributeModifier(
            createUuid("Strong Physique"),
            "Strong Physique",
            4.0,
            EntityAttributeModifier.Operation.ADDITION
        )
    )

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        val health = player.health
        super.postUnequipped(player, slot)
        if (health > player.maxHealth) {
            player.health = player.maxHealth
        }
    }
}
