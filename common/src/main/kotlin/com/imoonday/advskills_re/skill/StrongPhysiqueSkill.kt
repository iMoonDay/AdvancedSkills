package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.trigger.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class StrongPhysiqueSkill : PassiveSkill(
    Settings(
        id = "strong_physique",
        rarity = SkillRarity.SUPERB
    ), customToggles = true
), StopTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings.addParameter(
            name = "health_bonus",
            baseValue = 4.0,
            enhancementId = "value",
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT
        )
    }

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MAX_HEALTH to EntityAttributeModifier(
            createUuid("Strong Physique"),
            "Strong Physique",
            getDoubleParam("health_bonus", player, 4.0),
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
