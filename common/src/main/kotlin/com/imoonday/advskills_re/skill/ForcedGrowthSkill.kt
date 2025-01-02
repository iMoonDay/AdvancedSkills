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

    override fun initDefaultSettings(settings: Settings) {
        settings.addParameter(
            name = "range",
            baseValue = 5.0,
            enhancementId = "range",
            value = 1.0,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.FLOAT
        ).addParameter(
            name = "growth_count",
            baseValue = 1,
            enhancementId = "count",
            value = 1,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT
        ).addParameter(
            name = "success_chance",
            baseValue = 0.5f,
            enhancementId = "chance",
            value = 0.1f,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        val world = user.serverWorld
        val range = getDoubleParam("range", user, 5.0)
        val times = getIntParam("growth_count", user, 1)
        val chance = getFloatParam("success_chance", user, 0.5f)
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
        return UseResult.success(if (result > 0) message("success", result) else failedMessage())
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
}