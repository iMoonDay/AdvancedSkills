package com.imoonday.skills

import com.imoonday.components.startCooling
import com.imoonday.components.stopUsingSkill
import com.imoonday.triggers.AttributeTrigger
import com.imoonday.utils.SkillType
import com.imoonday.utils.UseResult
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand
import java.util.*
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

class ChargedSweepSkill : LongPressSkill(
    id = "charged_sweep",
    types = arrayOf(SkillType.ATTACK),
    cooldown = 9,
    rarity = Rarity.RARE,
), AttributeTrigger {
    override val maxPressTime: Int = 20 * 3
    override val attribute: Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            MOVEMENT_SPEED_UUID,
            "Charged Sweep Charging",
            -0.8,
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun onPress(player: ServerPlayerEntity): UseResult {
        addAttributes(player)
        return super.onPress(player)
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        removeAttributes(player)
        player.stopUsingSkill(this)
        player.world.getOtherEntities(player, player.boundingBox.expand(5.0)) { it.isLiving && it.isAlive }
            .map { it as LivingEntity }
            .filter {
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
                )) * (pressedTime.toFloat() / maxPressTime * 2)
                it.damage(player.damageSources.playerAttack(player), amount)
            }
        player.swingHand(Hand.MAIN_HAND, true)
        player.world.playSound(null, player.blockPos, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS)
        player.startCooling(this, pressedTime * 3)
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

    companion object {
        val MOVEMENT_SPEED_UUID: UUID = UUID.fromString("D4B7E033-3EAC-4A14-86E1-12D4D2D86B5A")
    }
}