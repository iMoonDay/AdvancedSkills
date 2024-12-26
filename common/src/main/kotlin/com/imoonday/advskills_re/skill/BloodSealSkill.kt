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
    id = "blood_seal",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 45,
    rarity = SkillRarity.EPIC,
    enhancements = setOf(
        SkillEnhancements.CHARGE_TIME,
        SkillEnhancements.CHARGE_SLOWDOWN,
        SkillEnhancements.DAMAGE,
        SkillEnhancements.DISTANCE,
        SkillEnhancements.STATUS_EFFECT_DURATION
    )
), AttributeTrigger, UsingRenderTrigger, CrosshairTrigger, TargetRenderTrigger, DangerTrigger {

    override val timeParameterName: String = "charge_time"

    init {
        addEnhanceableParameter(timeParameterName, 5 * 20, "time", -0.16f, Enhancement.Type.MULTIPLY, 5) { (it * 100).toInt() }

        addEnhancementTooltipWithArg(SkillEnhancements.DISTANCE) { it.level }
    }

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Blood Seal Charging"),
            "Blood Seal Charging",
            player.applyChargeSlowdownEnhancement(-0.25),
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
                    getEnhancedValue(player, SkillEnhancements.DAMAGE, 3f)
                )
                (it.entity as? LivingEntity)?.addStatusEffect(
                    StatusEffectInstance(
                        ModEffects.SERIOUS_INJURY.get(),
                        getEnhancedValue(player, SkillEnhancements.STATUS_EFFECT_DURATION, 7 * 20),
                    )
                )
                return UseResult.success()
            }
        return UseResult.fail(failedMessage())
    }

    private fun PlayerEntity.getRaycastDistance() =
        5.0 + this.getEnhancementLvl(SkillEnhancements.DISTANCE)

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