package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class MeteorShowerSkill : LongPressSkill(
    Settings(
        id = "meteor_shower",
        types = listOf(SkillType.ATTACK, SkillType.DESTRUCTION),
        cooldown = 120,
        rarity = SkillRarity.MYTHIC
    )
), AttributeTrigger, UsingRenderTrigger, DangerTrigger {

    init {
        settings
            .addParameter(PARAM_MIN_METEOR_COUNT, DEFAULT_MIN_METEOR_COUNT)
            .addParameter(PARAM_MAX_METEOR_COUNT, DEFAULT_MAX_METEOR_COUNT)
            .addParameter(
                name = PARAM_CHARGE_DURATION,
                baseValue = DEFAULT_CHARGE_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = -0.16,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT,
                genericText = true
            ).addParameter(
                name = PARAM_MOVEMENT_PENALTY,
                baseValue = DEFAULT_MOVEMENT_PENALTY,
                enhancementId = ENHANCEMENT_PENALTY,
                value = -0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_BONUS_METEOR_COUNT,
                baseValue = DEFAULT_BONUS_METEOR_COUNT,
                enhancementId = ENHANCEMENT_COUNT,
                value = 2,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = PARAM_IMPACT_RANGE,
                baseValue = DEFAULT_IMPACT_RANGE,
                enhancementId = ENHANCEMENT_RANGE,
                value = 2.0,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.FLOAT
            ).addParameter(
                name = PARAM_METEOR_SIZE,
                baseValue = DEFAULT_METEOR_SIZE,
                enhancementId = ENHANCEMENT_SIZE,
                value = 0.2f,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_FALL_SPEED,
                baseValue = DEFAULT_FALL_SPEED,
                enhancementId = ENHANCEMENT_SPEED,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Meteor Shower Charging"),
            "Meteor Shower Charging",
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
            return UseResult.fail(failedMessage)
        }
        val targetPos = player.raycast(512.0, 0f, false).pos
        val random = player.random
        val minCount = getIntParam(PARAM_MIN_METEOR_COUNT, player, DEFAULT_MIN_METEOR_COUNT)
        val maxCount = getIntParam(PARAM_MAX_METEOR_COUNT, player, DEFAULT_MAX_METEOR_COUNT, minCount)
        val bonusCount = getIntParam(PARAM_BONUS_METEOR_COUNT, player, DEFAULT_BONUS_METEOR_COUNT)
        val count = (minCount..maxCount).random() + bonusCount
        val range = getDoubleParam(PARAM_IMPACT_RANGE, player, DEFAULT_IMPACT_RANGE)
        val size = getFloatParam(PARAM_METEOR_SIZE, player, DEFAULT_METEOR_SIZE)
        val speed = getDoubleParam(PARAM_FALL_SPEED, player, DEFAULT_FALL_SPEED)

        for (i in 0 until count) {
            val x = targetPos.x + random.nextDouble() * range * 2 - range
            val z = targetPos.z + random.nextDouble() * range * 2 - range
            val r = (random.nextFloat() + 0.5f) * size
            player.world.spawnEntity(
                MeteoriteEntity(player.world, Vec3d(x, player.world.topY + r * 2.0, z), r, player).apply {
                    velocity = Vec3d(
                        random.nextDouble() * 0.2 - 0.1,
                        -2.0 * speed,
                        random.nextDouble() * 0.2 - 0.1
                    )
                }
            )
        }
        return UseResult.success()
    }

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_CHARGE_DURATION, player, DEFAULT_CHARGE_DURATION, 0)

    override fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean {
        if (player.isUsing()) player.startCooling(10)
        return true
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) =
        super<AttributeTrigger>.postUnequipped(player, slot)

    companion object {

        // Default Values
        private const val DEFAULT_MIN_METEOR_COUNT = 5
        private const val DEFAULT_MAX_METEOR_COUNT = 10
        private const val DEFAULT_CHARGE_DURATION = 10 * 20
        private const val DEFAULT_MOVEMENT_PENALTY = 0.5
        private const val DEFAULT_BONUS_METEOR_COUNT = 0
        private const val DEFAULT_IMPACT_RANGE = 10.0
        private const val DEFAULT_METEOR_SIZE = 1.0f
        private const val DEFAULT_FALL_SPEED = 1.0

        // Parameter Names
        private const val PARAM_MIN_METEOR_COUNT = "min_meteor_count"  // 最小陨石数量
        private const val PARAM_MAX_METEOR_COUNT = "max_meteor_count"  // 最大陨石数量
        private const val PARAM_CHARGE_DURATION = "charge_duration"  // 蓄力时间
        private const val PARAM_MOVEMENT_PENALTY = "movement_penalty"  // 移动速度惩罚
        private const val PARAM_BONUS_METEOR_COUNT = "bonus_meteor_count"  // 额外陨石数量
        private const val PARAM_IMPACT_RANGE = "impact_range"  // 陨石降落范围
        private const val PARAM_METEOR_SIZE = "meteor_size"  // 陨石大小
        private const val PARAM_FALL_SPEED = "fall_speed"  // 陨石下落速度

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应蓄力时间
        private const val ENHANCEMENT_PENALTY = "penalty"  // 对应移动惩罚
        private const val ENHANCEMENT_COUNT = "count"  // 对应陨石数量
        private const val ENHANCEMENT_RANGE = "range"  // 对应降落范围
        private const val ENHANCEMENT_SIZE = "size"  // 对应陨石大小
        private const val ENHANCEMENT_SPEED = "speed"  // 对应下落速度
    }
}