package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class StrongPhysiqueSkill : PassiveSkill(
    id = "strong_physique",
    rarity = SkillRarity.SUPERB
) {

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
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
