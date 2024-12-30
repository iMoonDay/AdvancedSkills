package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.effect.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.util.*
import net.minecraft.util.hit.*

class BloodSealSkill : LongPressSkill(
    Settings(
        id = "blood_seal",
        types = listOf(SkillType.ENHANCEMENT),
        cooldown = 45,
        rarity = SkillRarity.EPIC
    )
), AttributeTrigger, UsingRenderTrigger, CrosshairTrigger, TargetRenderTrigger, DangerTrigger {

    override val timeParamName: String = "charge_time"

    init {
        addEnhanceableParameter(
            name = timeParamName,
            baseValue = 5 * 20,
            enhancementId = "time",
            value = -0.16,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )

        addEnhanceableParameter(
            name = "charge_slowdown",
            baseValue = 0.25,
            enhancementId = "slowdown_reduction",
            value = -0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )

        addEnhanceableParameter(
            name = "damage",
            baseValue = 3f,
            enhancementId = "damage",
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )

        addEnhanceableParameter(
            name = "distance",
            baseValue = 5.0,
            enhancementId = "distance",
            value = 1.0,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5
        )

        addEnhanceableParameter(
            name = "status_effect_duration",
            baseValue = 7 * 20,
            enhancementId = "duration",
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )
    }

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Blood Seal Charging"),
            "Blood Seal Charging",
            -getDoubleParam("charge_slowdown", player, 0.25, 0.0, 1.0),
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun onPress(player: ServerPlayerEntity): UseResult {
        player.addAttributes()
        return super.onPress(player)
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.removeAttributes()
        player.stopUsing()
        if (pressedTime < getPersistTime(player)) {
            player.startCooling(10)
            return UseResult.fail(message("interrupt"))
        }
        player.swingHand(Hand.MAIN_HAND, true)
        player.raycastLivingEntity(player.getRaycastDistance())
            ?.takeIf { it.type == HitResult.Type.ENTITY }
            ?.let {
                it.entity.damage(
                    player.damageSources.playerAttack(player),
                    getFloatParam("damage", player, 3f)
                )
                (it.entity as? LivingEntity)?.addStatusEffect(
                    StatusEffectInstance(
                        ModEffects.SERIOUS_INJURY.get(),
                        getIntParam("status_effect_duration", player, 7 * 20, 0),
                    )
                )
                return UseResult.success()
            }
        return UseResult.fail(failedMessage())
    }

    private fun PlayerEntity.getRaycastDistance() =
        getDoubleParam("distance", this, 5.0, 0.0)

    override fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean {
        if (player.isUsing()) player.startCooling(10)
        return true
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) =
        super<AttributeTrigger>.postUnequipped(player, slot)

    override fun getCrosshair(player: PlayerEntity): Crosshair {
        player.run {
            if (!isUsing()) return Crosshairs.NONE
            if (raycastLivingEntity(player.getRaycastDistance())?.type == HitResult.Type.ENTITY) return Crosshairs.CROSS
        }
        return Crosshairs.NONE
    }

    override fun isTarget(clientPlayer: PlayerEntity, entity: LivingEntity): Boolean {
        if (!clientPlayer.isUsing()) return false
        return clientPlayer.raycastLivingEntity(clientPlayer.getRaycastDistance())?.entity == entity
    }
}