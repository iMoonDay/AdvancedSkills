package com.imoonday.skill

import com.imoonday.trigger.AttributeTrigger
import com.imoonday.trigger.UsingRenderTrigger
import com.imoonday.util.SkillSlot
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.playSound
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

class ChargedSweepSkill : LongPressSkill(
    id = "charged_sweep",
    types = listOf(SkillType.ATTACK),
    cooldown = 9,
    rarity = Rarity.RARE,
), AttributeTrigger, UsingRenderTrigger {

    override fun getMaxPressTime(): Int = 20 * 3
    override fun getAttributes(): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Charged Sweep Charging"),
            "Charged Sweep Charging",
            -0.8,
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
        player.removeAttributes()
        player.stopUsing()
        player.world.getNonSpectatingEntities(
            LivingEntity::class.java, player.boundingBox.expand(5.0)
        ).filter {
            (it.boundingBox.maxY >= player.boundingBox.minY
                && it.boundingBox.maxY <= player.boundingBox.maxY
                || it.boundingBox.minY <= player.boundingBox.maxY
                && it.boundingBox.minY >= player.boundingBox.minY)
                && player.calculateAngle(it) <= PI / 3
        }.forEach {
            val amount = (player.attributes.getValue(EntityAttributes.GENERIC_ATTACK_DAMAGE).toFloat()
                + EnchantmentHelper.getAttackDamage(
                player.mainHandStack,
                it.group
            )) * (pressedTime.toFloat() / getMaxPressTime() * 2)
            it.damage(player.damageSources.playerAttack(player), amount)
        }
        player.swingHand(Hand.MAIN_HAND, true)
        player.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP)
        player.startCooling(pressedTime * 3)
        player.mainHandStack.damage(1, player) { it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND) }
        return UseResult.consume()
    }

    private fun PlayerEntity.calculateAngle(entity: LivingEntity): Double {
        val vectorX = entity.x - x
        val vectorZ = entity.z - z
        val magnitude = sqrt(vectorX.pow(2) + vectorZ.pow(2))
        val vector = rotationVector.normalize()
        val product = vectorX * vector.x + vectorZ * vector.z
        return acos(product / magnitude)
    }

    override fun isDangerousTo(player: ServerPlayerEntity): Boolean = player.isUsing()
}