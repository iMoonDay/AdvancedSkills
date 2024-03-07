package com.imoonday.skill

import com.imoonday.util.SkillSlot
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

class StrongPhysiqueSkill : PassiveSkill(
    id = "strong_physique",
    rarity = Rarity.SUPERB
) {

    override fun getAttributes(): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MAX_HEALTH to EntityAttributeModifier(
            MAX_HEALTH_UUID,
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

    companion object {
        @JvmField
        val MAX_HEALTH_UUID: UUID = UUID.fromString("D6E0D579-E58A-4A7B-9D06-28A878CFA3A8")
    }
}
