package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.block.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.server.world.*
import net.minecraft.util.math.*
import net.minecraft.util.math.random.*

class ForcedGrowthSkill : Skill(
    Settings(
        id = "forced_growth",
        types = listOf(SkillType.UTILITY),
        cooldown = 30,
        rarity = SkillRarity.RARE
    )
) {

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings.addParameter(
            name = PARAM_GROWTH_RANGE,
            baseValue = DEFAULT_GROWTH_RANGE,
            enhancementId = ENHANCEMENT_RANGE,
            value = 1.0,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.FLOAT
        ).addParameter(
            name = PARAM_GROWTH_TIMES,
            baseValue = DEFAULT_GROWTH_TIMES,
            enhancementId = ENHANCEMENT_TIMES,
            value = 1,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT
        ).addParameter(
            name = PARAM_GROWTH_CHANCE,
            baseValue = DEFAULT_GROWTH_CHANCE,
            enhancementId = ENHANCEMENT_CHANCE,
            value = 0.1f,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        val world = user.serverWorld
        val range = getDoubleParam(PARAM_GROWTH_RANGE, user, DEFAULT_GROWTH_RANGE)
        val times = getIntParam(PARAM_GROWTH_TIMES, user, DEFAULT_GROWTH_TIMES)
        val chance = getFloatParam(PARAM_GROWTH_CHANCE, user, DEFAULT_GROWTH_CHANCE)
        val random = user.random
        val result = user.boundingBox.expand(range)
            .blockPosSet
            .asSequence()
            .mapNotNull {
                val state = world.getBlockState(it)
                if (state.block is CropBlock) it to state else null
            }.count {
                tryGrow(world, it.first, it.second, times, chance, random).also { result ->
                    if (result) {
                        user.spawnParticles(
                            ParticleTypes.COMPOSTER,
                            false,
                            it.first.toCenterPos(),
                            5,
                            0.0,
                            0.5,
                            0.0,
                            0.1
                        )
                    }
                }
            }
        return UseResult.success(if (result > 0) message("success", result) else failedMessage)
    }

    private fun tryGrow(
        world: ServerWorld,
        pos: BlockPos,
        state: BlockState,
        times: Int,
        chance: Float,
        random: Random
    ): Boolean {
        val block = state.block
        if (block !is CropBlock || !block.isFertilizable(world, pos, state, world.isClient)) return false
        if (!block.canGrow(world, world.random, pos, state)) return false

        var grow = false
        for (i in 0 until times) {
            if (random.nextFloat() > chance) continue

            block.grow(world, world.random, pos, state)
            grow = true
        }

        return grow
    }

    companion object {

        // Default Values
        private const val DEFAULT_GROWTH_RANGE = 5.0
        private const val DEFAULT_GROWTH_TIMES = 1
        private const val DEFAULT_GROWTH_CHANCE = 0.5f

        // Parameter Names
        private const val PARAM_GROWTH_RANGE = "growth_range"  // 生长范围
        private const val PARAM_GROWTH_TIMES = "growth_times"  // 生长次数
        private const val PARAM_GROWTH_CHANCE = "growth_chance"  // 生长概率

        // Enhancement IDs
        private const val ENHANCEMENT_RANGE = "range"  // 对应生长范围
        private const val ENHANCEMENT_TIMES = "times"  // 对应生长次数
        private const val ENHANCEMENT_CHANCE = "chance"  // 对应生长概率
    }
}