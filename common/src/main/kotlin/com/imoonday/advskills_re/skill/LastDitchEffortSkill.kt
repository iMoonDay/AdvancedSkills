package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class LastDitchEffortSkill : Skill(
    Settings(
        id = "last_ditch_effort",
        types = listOf(SkillType.PASSIVE),
        cooldown = 180,
        rarity = SkillRarity.SUPERB
    )
), DamageTrigger, AutoStopTrigger, AttackTrigger,
    AttributeTrigger, AutoTrigger, DeathTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter("healing_sound", ModSounds.HEAL)
            .addParameter("health_threshold", 0.3f)
            .addParameter(
                name = timeParamName,
                baseValue = 15 * 20,
                enhancementId = "time",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "movement_speed",
                baseValue = 0.4,
                enhancementId = "speed",
                value = 0.06,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "damage_bonus",
                baseValue = 1.0f,
                enhancementId = "damage",
                value = 0.2f,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "healing_amount_multiplier",
                baseValue = 0.5f,
                enhancementId = "healing_multiplier",
                value = 0.1f,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "damage_taken_bonus",
                baseValue = 1.0f,
                enhancementId = "damage_taken",
                value = -0.2f,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "healing_amount",
                baseValue = 0.2f,
                enhancementId = "healing",
                value = 0.1f,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Last Ditch Effort"),
            "Last Ditch Effort",
            getDoubleParam("movement_speed", player, 0.4, 0.0),
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        super<AutoStopTrigger>.postUnequipped(player, slot)
        super<AttributeTrigger>.postUnequipped(player, slot)
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)

    override fun onAttack(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        target: LivingEntity,
    ): Float = if (!player.isUsing()) amount
    else (amount + 1) * getFloatParam("damage_bonus", player, 1.0f)

    override fun shouldStart(player: ServerPlayerEntity): Boolean =
        if (player.isReady() && !player.isDead) {
            val threshold = getFloatParam("health_threshold", player, 0.3f, 0.0f)
            if ((player.health / player.maxHealth) < threshold) {
                val healingBonus = getFloatParam("healing_amount", player, 0.2f, 0.0f)
                player.health = player.maxHealth * (threshold + healingBonus)
                player.playSoundFromParam("healing_sound", ModSounds.HEAL.get())
                player.addAttributes()
                true
            } else false
        } else false

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float = if (!player.isUsing()) amount
    else amount + getFloatParam("damage_taken_bonus", player, 1.0f)

    override fun onStop(player: ServerPlayerEntity) {
        player.startCooling()
        player.removeAttributes()
        super.onStop(player)
    }

    override fun onDeath(player: ServerPlayerEntity, source: DamageSource) {
        super.onDeath(player, source)
        if (player.isUsing()) {
            player.startCooling()
        }
    }
}