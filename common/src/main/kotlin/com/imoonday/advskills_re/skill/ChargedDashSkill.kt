package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.player.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class ChargedDashSkill : LongPressSkill(
    Settings(
        id = "charged_dash",
        types = listOf(SkillType.MOVEMENT),
        cooldown = 15,
        rarity = SkillRarity.SUPERB
    )
), AttributeTrigger {

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_CHARGE_TIME, player, DEFAULT_CHARGE_TIME, 0)

    init {
        settings.addParameter(
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
            name = PARAM_DASH_FORCE,
            baseValue = DEFAULT_DASH_FORCE,
            enhancementId = ENHANCEMENT_FORCE,
            value = 0.2,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT
        )
    }

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Charged Dash Charging"),
            "Charged Dash Charging",
            -getDoubleParam(PARAM_MOVEMENT_PENALTY, player, DEFAULT_MOVEMENT_PENALTY, 0.0, 1.0),
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun onPress(player: ServerPlayerEntity): UseResult {
        player.addAttributes()
        return super.onPress(player)
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.run {
            stopUsing()
            removeAttributes()
            val multiplier = getDoubleParam(PARAM_DASH_FORCE, player, DEFAULT_DASH_FORCE)
            velocity =
                rotationVector.normalize().multiply(2.0 * pressedTime / getMaxUseTime(player) * multiplier)
            updateVelocity()
            spawnParticles(ParticleTypes.CLOUD, false, pos, 10, 0.5, 0.0, 0.5, 0.1)
        }
        return UseResult.success()
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) =
        super<AttributeTrigger>.postUnequipped(player, slot)

    companion object {

        // Default Values
        private const val DEFAULT_CHARGE_TIME = 3 * 20
        private const val DEFAULT_MOVEMENT_PENALTY = 0.2
        private const val DEFAULT_DASH_FORCE = 1.0

        // Parameter Names
        private const val PARAM_CHARGE_TIME = "charge_time"  // 蓄力时间
        private const val PARAM_MOVEMENT_PENALTY = "movement_penalty"  // 移动速度惩罚
        private const val PARAM_DASH_FORCE = "dash_force"  // 冲刺力度

        // Enhancement IDs
        private const val ENHANCEMENT_CHARGE_TIME = "charge_time"  // 对应蓄力时间
        private const val ENHANCEMENT_MOVEMENT = "movement"  // 对应移动速度
        private const val ENHANCEMENT_FORCE = "force"  // 对应冲刺力度
    }
}