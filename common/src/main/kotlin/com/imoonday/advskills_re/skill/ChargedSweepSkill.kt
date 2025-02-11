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

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings
            .addParameter(PARAM_DAMAGE_ITEM, DEFAULT_DAMAGE_ITEM, ENHANCEMENT_NO_DAMAGE)
            .addParameter(
                name = PARAM_CHARGE_TIME,
                baseValue = DEFAULT_CHARGE_TIME,
                enhancementId = ENHANCEMENT_CHARGE_TIME,
                value = -0.16,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT,
                genericText = true
            ).addParameter(
                name = PARAM_MOVEMENT_PENALTY,
                baseValue = DEFAULT_MOVEMENT_PENALTY,
                enhancementId = ENHANCEMENT_MOVEMENT,
                value = -0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_SWEEP_RANGE,
                baseValue = DEFAULT_SWEEP_RANGE,
                enhancementId = ENHANCEMENT_RANGE,
                value = 1.0,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.FLOAT
            ).addParameter(
                name = PARAM_DAMAGE_BOOST,
                baseValue = DEFAULT_DAMAGE_BOOST,
                enhancementId = ENHANCEMENT_DAMAGE,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT,
                genericText = true
            )
    }

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Charged Sweep Charging"),
            "Charged Sweep Charging",
            -getDoubleParam(PARAM_MOVEMENT_PENALTY, player, DEFAULT_MOVEMENT_PENALTY, 0.0, 1.0),
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
        val range = getDoubleParam(PARAM_SWEEP_RANGE, player, DEFAULT_SWEEP_RANGE)
        val baseDamage = player.attributes.getValue(EntityAttributes.GENERIC_ATTACK_DAMAGE).toFloat()
        val multiplier =
            pressedTime.toFloat() / getMaxUseTime(player) * 2f * getFloatParam(
                PARAM_DAMAGE_BOOST, player, DEFAULT_DAMAGE_BOOST
            )
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
        if (getBooleanParam(PARAM_DAMAGE_ITEM, player, DEFAULT_DAMAGE_ITEM)) {
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

    companion object {

        // Default Values
        private const val DEFAULT_CHARGE_TIME = 3 * 20
        private const val DEFAULT_MOVEMENT_PENALTY = 0.8
        private const val DEFAULT_SWEEP_RANGE = 5.0
        private const val DEFAULT_DAMAGE_BOOST = 1.0f
        private const val DEFAULT_DAMAGE_ITEM = true

        // Parameter Names
        private const val PARAM_DAMAGE_ITEM = "damage_item"  // 是否消耗物品耐久
        private const val PARAM_CHARGE_TIME = "charge_time"  // 蓄力时间
        private const val PARAM_MOVEMENT_PENALTY = "movement_penalty"  // 移动速度惩罚
        private const val PARAM_SWEEP_RANGE = "sweep_range"  // 横扫范围
        private const val PARAM_DAMAGE_BOOST = "damage_boost"  // 伤害倍率

        // Enhancement IDs
        private const val ENHANCEMENT_NO_DAMAGE = "no_item_damage"  // 对应不消耗耐久
        private const val ENHANCEMENT_CHARGE_TIME = "charge_time"  // 对应蓄力时间
        private const val ENHANCEMENT_MOVEMENT = "movement"  // 对应移动速度
        private const val ENHANCEMENT_RANGE = "range"  // 对应横扫范围
        private const val ENHANCEMENT_DAMAGE = "damage"  // 对应伤害倍率
    }
}