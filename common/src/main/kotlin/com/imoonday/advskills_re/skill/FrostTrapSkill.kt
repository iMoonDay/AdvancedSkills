package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.block.entity.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.block.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class FrostTrapSkill : Skill(
    id = "frost_trap",
    types = listOf(SkillType.CONTROL),
    cooldown = 8,
    rarity = SkillRarity.EPIC,
    enhancements = setOf(
        SkillEnhancements.RANGE,
        SkillEnhancements.EFFECT_COUNT,
        SkillEnhancements.STATUS_EFFECT_DURATION
    )
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        val world = user.world
        val pos = user.blockPos
        val range = user.getEnhancementLvl(SkillEnhancements.RANGE)
        val times = 1 + user.getEnhancementLvl(SkillEnhancements.EFFECT_COUNT)
        var success = false
        val trapBlock = ModBlocks.FROST_TRAP.get()
        val defaultState = trapBlock.defaultState
        val modifyDuration: (Int) -> Int = { getEnhancedValue(user, SkillEnhancements.STATUS_EFFECT_DURATION, it) }
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
                    blockEntity.duration = modifyDuration(blockEntity.duration)
                }
                blockEntity.markDirty()
                success = true
            }
        }
        return if (success) UseResult.success() else UseResult.fail(failedMessage())
    }
}