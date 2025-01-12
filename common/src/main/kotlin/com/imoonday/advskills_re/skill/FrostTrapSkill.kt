package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.block.entity.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.block.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class FrostTrapSkill : Skill(
    Settings(
        id = "frost_trap",
        types = listOf(SkillType.CONTROL),
        cooldown = 8,
        rarity = SkillRarity.EPIC
    )
) {

    init {
        settings.addParameter(
            name = PARAM_TRAP_RANGE,
            baseValue = DEFAULT_TRAP_RANGE,
            enhancementId = ENHANCEMENT_RANGE,
            value = 1,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 3,
            descArg = Enhancement.ArgFormatter.INT
        ).addParameter(
            name = PARAM_TRAP_COUNT,
            baseValue = DEFAULT_TRAP_COUNT,
            enhancementId = ENHANCEMENT_COUNT,
            value = 1,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT
        ).addParameter(
            name = PARAM_FREEZE_DURATION,
            baseValue = DEFAULT_FREEZE_DURATION,
            enhancementId = ENHANCEMENT_DURATION,
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        val world = user.world
        val pos = user.blockPos
        val range = getIntParam(PARAM_TRAP_RANGE, user, DEFAULT_TRAP_RANGE)
        val times = getIntParam(PARAM_TRAP_COUNT, user, DEFAULT_TRAP_COUNT)
        var success = false
        val trapBlock = ModBlocks.FROST_TRAP.get()
        val defaultState = trapBlock.defaultState
        val duration = getIntParam(PARAM_FREEZE_DURATION, user, DEFAULT_FREEZE_DURATION)
        val uuid = user.uuid

        BlockPos.iterateOutwards(pos, range, 0, range).forEach {
            val wasTrap = world.getBlockEntity(it) is FrostTrapBlockEntity
            for (i in 0 until times) {
                val state = world.getBlockState(it)
                val canPlace = (state.isAir || state.isOf(trapBlock)) && trapBlock.canPlaceAt(state, world, it)
                if (!canPlace) return@forEach

                val layers =
                    if (state.contains(SnowBlock.LAYERS)) (state.get(SnowBlock.LAYERS) + 1).coerceAtMost(8) else 1
                val newState = defaultState.with(SnowBlock.LAYERS, layers)

                world.setBlockState(it, newState)
            }

            val blockEntity = world.getBlockEntity(it)
            if (blockEntity is FrostTrapBlockEntity) {
                blockEntity.placer = uuid
                if (!wasTrap) {
                    blockEntity.duration = duration
                }
                blockEntity.markDirty()
                success = true
            }
        }
        return if (success) UseResult.success() else UseResult.fail(failedMessage)
    }

    companion object {

        // Default Values
        private const val DEFAULT_TRAP_RANGE = 0
        private const val DEFAULT_TRAP_COUNT = 1
        private const val DEFAULT_FREEZE_DURATION = 10

        // Parameter Names
        private const val PARAM_TRAP_RANGE = "trap_range"  // 陷阱范围
        private const val PARAM_TRAP_COUNT = "trap_count"  // 陷阱数量
        private const val PARAM_FREEZE_DURATION = "freeze_duration"  // 冻结时长

        // Enhancement IDs
        private const val ENHANCEMENT_RANGE = "range"  // 对应陷阱范围
        private const val ENHANCEMENT_COUNT = "count"  // 对应陷阱数量
        private const val ENHANCEMENT_DURATION = "duration"  // 对应冻结时长
    }
}