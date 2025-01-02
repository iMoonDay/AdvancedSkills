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
            name = "freeze_duration",
            baseValue = 10,
            enhancementId = "duration",
            value = 0.2,
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
        var success = false
        val trapBlock = ModBlocks.FROST_TRAP.get()
        val defaultState = trapBlock.defaultState
        val duration = getIntParam("freeze_duration", user, 10)
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
        return if (success) UseResult.success() else UseResult.fail(failedMessage())
    }
}