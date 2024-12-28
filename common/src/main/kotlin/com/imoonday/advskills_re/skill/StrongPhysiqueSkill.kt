package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.trigger.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class StrongPhysiqueSkill : PassiveSkill(
    Settings(
        id = "strong_physique",
        rarity = SkillRarity.SUPERB
    ), customToggles = true
//    enhancements = setOf(SkillEnhancements.EFFECT_VALUE)
), StopTrigger {

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MAX_HEALTH to EntityAttributeModifier(
            createUuid("Strong Physique"),
            "Strong Physique",
            4.0 * (1.0 + player.getEnhancementLvl(SkillEnhancements.EFFECT_VALUE) * 0.2),
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

    override fun postStop(player: PlayerEntity) {
        super.postStop(player)
        if (player.health > player.maxHealth) {
            player.health = player.maxHealth
        }
    }
}
