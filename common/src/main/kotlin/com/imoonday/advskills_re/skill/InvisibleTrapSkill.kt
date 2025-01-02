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

    override fun initDefaultSettings(settings: Settings) {
        settings.addParameter(
            name = "range",
            baseValue = 0,
            enhancementId = "range",
            value = 1,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT
        ).addParameter(
            name = "trap_count",
            baseValue = 1,
            enhancementId = "count",
            value = 1,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT
        ).addParameter(
            name = "damage",
            baseValue = 2.0f,
            enhancementId = "damage",
            value = 0.2f,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        val world = user.world
        val pos = user.blockPos
        val range = getIntParam("range", user, 0)
        val times = getIntParam("trap_count", user, 1)
        val trapBlock = ModBlocks.INVISIBLE_TRAP.get()
        val defaultState = trapBlock.defaultState
        val damage = getFloatParam("damage", user, 2.0f)
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

        return if (success) UseResult.success() else UseResult.fail(failedMessage())
    }
}