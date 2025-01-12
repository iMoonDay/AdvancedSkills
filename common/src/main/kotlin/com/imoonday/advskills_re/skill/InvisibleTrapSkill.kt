package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.block.*
import com.imoonday.advskills_re.block.entity.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.UseResult
import net.minecraft.fluid.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class InvisibleTrapSkill : Skill(
    Settings(
        id = "invisible_trap",
        types = listOf(SkillType.ATTACK),
        cooldown = 8,
        rarity = SkillRarity.UNCOMMON
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
            name = PARAM_TRAP_DAMAGE,
            baseValue = DEFAULT_TRAP_DAMAGE,
            enhancementId = ENHANCEMENT_DAMAGE,
            value = 0.2f,
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
        val trapBlock = ModBlocks.INVISIBLE_TRAP.get()
        val defaultState = trapBlock.defaultState
        val damage = getFloatParam(PARAM_TRAP_DAMAGE, user, DEFAULT_TRAP_DAMAGE)
        val uuid = user.uuid

        var success = false
        BlockPos.iterateOutwards(pos, range, 0, range).forEach {
            val wasTrap = world.getBlockEntity(it) is InvisibleTrapBlockEntity
            for (i in 0 until times) {
                val state = world.getBlockState(it)
                val canPlace = (state.isAir || state.isOf(trapBlock)) && trapBlock.canPlaceAt(state, world, it)
                if (!canPlace) return@forEach

                val hasFluid = world.getFluidState(it).fluid == Fluids.WATER
                val newState = defaultState.with(InvisibleTrapBlock.WATERLOGGED, hasFluid)

                world.setBlockState(it, newState)
            }

            val blockEntity = world.getBlockEntity(it)
            if (blockEntity is InvisibleTrapBlockEntity) {
                blockEntity.placer = uuid
                if (!wasTrap) {
                    blockEntity.damage = damage
                }
                blockEntity.markDirty()
                success = true
            }
        }

        return UseResult.of(success, failMessage = failedMessage)
    }

    companion object {

        // Default Values
        private const val DEFAULT_TRAP_RANGE = 0
        private const val DEFAULT_TRAP_COUNT = 1
        private const val DEFAULT_TRAP_DAMAGE = 2.0f

        // Parameter Names
        private const val PARAM_TRAP_RANGE = "trap_range"  // 陷阱范围
        private const val PARAM_TRAP_COUNT = "trap_count"  // 陷阱数量
        private const val PARAM_TRAP_DAMAGE = "trap_damage"  // 陷阱伤害

        // Enhancement IDs
        private const val ENHANCEMENT_RANGE = "range"  // 对应陷阱范围
        private const val ENHANCEMENT_COUNT = "count"  // 对应陷阱数量
        private const val ENHANCEMENT_DAMAGE = "damage"  // 对应陷阱伤害
    }
}