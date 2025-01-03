package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.enchantment.*
import net.minecraft.entity.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.util.*
import kotlin.math.*

class ChargedSweepSkill : LongPressSkill(
    Settings(
        id = "charged_sweep",
        types = listOf(SkillType.ATTACK),
        cooldown = 9,
        rarity = SkillRarity.RARE
    )
), AttributeTrigger, UsingRenderTrigger, DangerTrigger {

    override fun getMaxUseTime(player: PlayerEntity): Int = getIntParam("charge_time", player, 3 * 20, 0)

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter("damage_item", true, "no_item_damage")
            .addParameter(
                name = "charge_time",
                baseValue = 3 * 20,
                enhancementId = "time",
                value = -0.16,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "charge_slowdown",
                baseValue = 0.8,
                enhancementId = "slowdown_reduction",
                value = -0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "range",
                baseValue = 5.0,
                enhancementId = "range",
                value = 1.0,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.FLOAT
            ).addParameter(
                name = "damage_multiplier",
                baseValue = 1.0f,
                enhancementId = "multiplier",
                value = 0.2f,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Charged Sweep Charging"),
            "Charged Sweep Charging",
            -getDoubleParam("charge_slowdown", player, 0.8, 0.0, 1.0),
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
        val range = getDoubleParam("range", player, 5.0)
        val baseDamage = player.attributes.getValue(EntityAttributes.GENERIC_ATTACK_DAMAGE).toFloat()
        val multiplier =
            pressedTime.toFloat() / getMaxUseTime(player) * 2f * getFloatParam("damage_multiplier", player, 1f)
        val stack = player.mainHandStack
        player.world.getOtherEntities(player, player.boundingBox.expand(range)) {
            it is LivingEntity &&
                (it.boundingBox.maxY >= player.boundingBox.minY
                    && it.boundingBox.maxY <= player.boundingBox.maxY
                    || it.boundingBox.minY <= player.boundingBox.maxY
                    && it.boundingBox.minY >= player.boundingBox.minY)
                && player.calculateAngle(it) <= PI / 3
        }.filterIsInstance<LivingEntity>().forEach {
            val amount = (baseDamage + EnchantmentHelper.getAttackDamage(stack, it.group)) * multiplier
            it.damage(player.damageSources.playerAttack(player), amount)
        }
        player.swingHand(Hand.MAIN_HAND, true)
        player.spawnSweepAttackParticles()
        player.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP)
        player.startCooling(pressedTime * 3)
        if (getBooleanParam("damage_item", player, true)) {
            stack.damage(1, player) { it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND) }
        }
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
}