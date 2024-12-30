package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.UseResult
import com.imoonday.advskills_re.util.isUsing
import net.minecraft.entity.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class ActiveDefenseSkill : LongPressSkill(
    Settings(
        id = "active_defense",
        types = listOf(SkillType.DEFENSE),
        cooldown = 10,
        rarity = SkillRarity.SUPERB
    )
), DamageTrigger, AttributeTrigger, UsingRenderTrigger {

    init {
        addEnhanceableParameter(
            name = timeParamName,
            baseValue = 5 * 20,
            enhancementId = "time",
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )

        addEnhanceableParameter(
            name = "damage_reduction",
            baseValue = 0.2f,
            enhancementId = "reduction_value",
            value = 0.06f,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )

        addEnhanceableParameter(
            name = "charge_slowdown",
            baseValue = 0.5,
            enhancementId = "slowdown_multiplier",
            value = -0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )
    }

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Active Defense"),
            "Active Defense",
            -getDoubleParam("charge_slowdown", player, 0.5, 0.0, 1.0),
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) =
        super<AttributeTrigger>.postUnequipped(player, slot)

    override fun onPress(player: ServerPlayerEntity): UseResult {
        player.addAttributes()
        return super.onPress(player)
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopAndCooldown()
        if (pressedTime.toFloat() / getPersistTime(player) < 0.5f) {
            player.modifyCooldown { it / 2 }
        }
        player.removeAttributes()
        return UseResult.consume()
    }

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float = if (!player.isUsing()) amount
    else amount * (1f - getFloatParam("damage_reduction", player, 0.2f, 0f, 1f))

    override fun shouldRenderFeature(target: PlayerEntity, clientPlayer: PlayerEntity): Boolean =
        target.isUsing() && !target.isUsing(Skills.ABSOLUTE_DEFENSE)
}