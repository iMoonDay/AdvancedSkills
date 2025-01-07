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

    init {
        settings.addParameter(
            name = PARAM_HEALTH_BOOST,
            baseValue = DEFAULT_HEALTH_BOOST,
            enhancementId = ENHANCEMENT_BOOST,
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
            getDoubleParam(PARAM_HEALTH_BOOST, player, DEFAULT_HEALTH_BOOST),
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

    companion object {
        // Default Values
        private const val DEFAULT_HEALTH_BOOST = 4.0

        // Parameter Names
        private const val PARAM_HEALTH_BOOST = "health_boost"  // 生命值提升

        // Enhancement IDs
        private const val ENHANCEMENT_BOOST = "boost"  // 对应提升值
    }
}
