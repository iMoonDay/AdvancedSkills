package com.imoonday.skill

import com.imoonday.init.ModEffects
import com.imoonday.trigger.AttributeTrigger
import com.imoonday.trigger.CrosshairTrigger
import com.imoonday.trigger.UsingRenderTrigger
import com.imoonday.util.*
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.hit.HitResult

class BloodSealSkill : LongPressSkill(
    id = "blood_seal",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 45,
    rarity = Rarity.EPIC,
), AttributeTrigger, UsingRenderTrigger, CrosshairTrigger {

    override fun getMaxPressTime(): Int = 20 * 5

    override fun getAttributes(): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Blood Seal Charging"),
            "Blood Seal Charging",
            -0.25,
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
        if (pressedTime < getMaxPressTime()) {
            player.startCooling(10)
            return UseResult.fail(message("interrupt"))
        }
        player.swingHand(Hand.MAIN_HAND, true)
        player.raycastLivingEntity(5.0)?.takeIf { it.type == HitResult.Type.ENTITY }?.let {
            it.entity.damage(player.damageSources.playerAttack(player), 3f)
            (it.entity as? LivingEntity)?.addStatusEffect(StatusEffectInstance(ModEffects.SERIOUS_INJURY, 20 * 7))
            return UseResult.success()
        }
        return UseResult.fail(failedMessage())
    }

    override fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean {
        if (player.isUsing()) player.startCooling(10)
        return true
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) =
        super<AttributeTrigger>.postUnequipped(player, slot)

    override fun isDangerousTo(player: ServerPlayerEntity): Boolean = player.isUsing()

    override fun getCrosshair(): Crosshair {
        clientPlayer?.run {
            if (!isUsing()) return Crosshairs.NONE
            if (raycastLivingEntity(5.0)?.type == HitResult.Type.ENTITY) return Crosshairs.CROSS
        }
        return Crosshairs.NONE
    }
}