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

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(
                name = PARAM_CHARGE_TIME,
                baseValue = DEFAULT_CHARGE_TIME,
                enhancementId = ENHANCEMENT_CHARGE_TIME,
                value = -0.16,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_MOVEMENT_PENALTY,
                baseValue = DEFAULT_MOVEMENT_PENALTY,
                enhancementId = ENHANCEMENT_MOVEMENT,
                value = -0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_DAMAGE,
                baseValue = DEFAULT_DAMAGE,
                enhancementId = ENHANCEMENT_DAMAGE,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT,
                genericText = true
            ).addParameter(
                name = PARAM_DISTANCE,
                baseValue = DEFAULT_DISTANCE,
                enhancementId = ENHANCEMENT_DISTANCE,
                value = 1.0,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.FLOAT
            ).addParameter(
                name = PARAM_EFFECT_DURATION,
                baseValue = DEFAULT_EFFECT_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun getMaxUseTime(player: PlayerEntity): Int = 
        getIntParam(PARAM_CHARGE_TIME, player, DEFAULT_CHARGE_TIME, 0)

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Blood Seal Charging"),
            "Blood Seal Charging",
            -getDoubleParam(PARAM_MOVEMENT_PENALTY, player, DEFAULT_MOVEMENT_PENALTY, 0.0, 1.0),
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
        if (pressedTime < getMaxUseTime(player)) {
            player.startCooling(10)
            return UseResult.fail(message("interrupt"))
        }
        player.swingHand(Hand.MAIN_HAND, true)
        player.raycastLivingEntity(player.getRaycastDistance())
            ?.takeIf { it.type == HitResult.Type.ENTITY }
            ?.let {
                it.entity.damage(
                    player.damageSources.playerAttack(player),
                    getFloatParam(PARAM_DAMAGE, player, DEFAULT_DAMAGE)
                )
                (it.entity as? LivingEntity)?.addStatusEffect(
                    StatusEffectInstance(
                        ModEffects.SERIOUS_INJURY.get(),
                        getIntParam(PARAM_EFFECT_DURATION, player, DEFAULT_EFFECT_DURATION, 0),
                    )
                )
                return UseResult.success()
            }
        return UseResult.fail(failedMessage())
    }

    private fun PlayerEntity.getRaycastDistance() =
        getDoubleParam(PARAM_DISTANCE, this, DEFAULT_DISTANCE, 0.0)

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

    companion object {
        // Default Values
        private const val DEFAULT_CHARGE_TIME = 5 * 20
        private const val DEFAULT_MOVEMENT_PENALTY = 0.25
        private const val DEFAULT_DAMAGE = 3f
        private const val DEFAULT_DISTANCE = 5.0
        private const val DEFAULT_EFFECT_DURATION = 7 * 20

        // Parameter Names
        private const val PARAM_CHARGE_TIME = "charge_time"  // 蓄力时间
        private const val PARAM_MOVEMENT_PENALTY = "movement_penalty"  // 移动速度惩罚
        private const val PARAM_DAMAGE = "damage"  // 伤害值
        private const val PARAM_DISTANCE = "attack_range"  // 攻击范围
        private const val PARAM_EFFECT_DURATION = "effect_duration"  // 效果持续时间

        // Enhancement IDs
        private const val ENHANCEMENT_CHARGE_TIME = "charge_time"  // 对应蓄力时间
        private const val ENHANCEMENT_MOVEMENT = "movement"  // 对应移动速度
        private const val ENHANCEMENT_DAMAGE = "damage"  // 对应伤害值
        private const val ENHANCEMENT_DISTANCE = "distance"  // 对应攻击范围
        private const val ENHANCEMENT_DURATION = "duration"  // 对应效果持续时间
    }
}