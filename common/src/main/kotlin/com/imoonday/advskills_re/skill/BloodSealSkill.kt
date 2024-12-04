package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.trigger.renderer.*
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
    rarity = Rarity.EPIC,
), AttributeTrigger, UsingRenderTrigger, CrosshairTrigger, TargetRenderTrigger {

    override fun getMaxPressTime(): Int = 5 * 20

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
            (it.entity as? LivingEntity)?.addStatusEffect(
                StatusEffectInstance(
                    ModEffects.SERIOUS_INJURY.get(),
                    7 * 20
                )
            )
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

    override fun isDangerous(player: ServerPlayerEntity): Boolean = player.isUsing()

    override fun getCrosshair(player: PlayerEntity): Crosshair {
        player.run {
            if (!isUsing()) return Crosshairs.NONE
            if (raycastLivingEntity(5.0)?.type == HitResult.Type.ENTITY) return Crosshairs.CROSS
        }
        return Crosshairs.NONE
    }

    override fun isTarget(player: PlayerEntity, entity: LivingEntity): Boolean {
        if (!player.isUsing()) return false
        return player.raycastLivingEntity(5.0)?.entity == entity
    }
}