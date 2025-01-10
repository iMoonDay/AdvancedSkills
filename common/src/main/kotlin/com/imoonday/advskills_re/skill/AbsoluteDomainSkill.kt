package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.block.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class AbsoluteDomainSkill : Skill(
    Settings(
        id = "absolute_domain",
        types = listOf(SkillType.DESTRUCTION),
        cooldown = 15,
        rarity = SkillRarity.RARE
    )
), AutoStopTrigger {

    init {
        settings
            .addParameter(PARAM_MIN_HARDNESS, DEFAULT_MIN_HARDNESS)
            .addParameter(PARAM_MAX_HARDNESS, DEFAULT_MAX_HARDNESS)
            .addParameter(PARAM_ABOVE_PLAYER, DEFAULT_ABOVE_PLAYER_Y)
            .addParameter(
                name = PARAM_DURATION,
                baseValue = DEFAULT_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_RANGE,
                baseValue = DEFAULT_RANGE,
                enhancementId = ENHANCEMENT_RANGE,
                value = 1.0,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 3,
                descArg = Enhancement.ArgFormatter.FLOAT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this, NbtCompound().apply {
        putDouble(NBT_RANGE, getDoubleParam(PARAM_RANGE, user, DEFAULT_RANGE))
        putFloat(NBT_MIN_HARDNESS, getFloatParam(PARAM_MIN_HARDNESS, user, DEFAULT_MIN_HARDNESS))
        putFloat(NBT_MAX_HARDNESS, getFloatParam(PARAM_MAX_HARDNESS, user, DEFAULT_MAX_HARDNESS))
        putBoolean(NBT_ABOVE_PLAYER_Y, getBooleanParam(PARAM_ABOVE_PLAYER, user, DEFAULT_ABOVE_PLAYER_Y))
    })

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_DURATION, player, DEFAULT_DURATION, 0)

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (!player.isUsing()) return

        val world = player.world
        val data = player.getActiveData()
        val range = data.getDouble(NBT_RANGE)
        val hardnessRange = data.getFloat(NBT_MIN_HARDNESS)..<data.getFloat(NBT_MAX_HARDNESS)
        val abovePlayerY = data.getBoolean(NBT_ABOVE_PLAYER_Y)

        player.boundingBox.expand(range).blockPosSet.filter {
            val hardness = world.getBlockState(it).getHardness(world, it)
            hardness in hardnessRange && (!abovePlayerY || it.y >= player.blockY)
        }.forEach {
            val hardness = world.getBlockState(it).getHardness(world, it).toInt()
            if (world.breakBlock(it, true, player)) {
                val centerPos = it.toCenterPos()
                player.spawnParticles(ParticleTypes.SMOKE, false, centerPos, hardness, 0.5, 0.5, 0.5, 0.1)
            }
        }
    }

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }

    companion object {

        // Default Values
        private const val DEFAULT_DURATION = 3 * 20
        private const val DEFAULT_RANGE = 1.0
        private const val DEFAULT_MIN_HARDNESS = 0f
        private val DEFAULT_MAX_HARDNESS = Blocks.OBSIDIAN.hardness
        private const val DEFAULT_ABOVE_PLAYER_Y = true

        // NBT Keys
        private const val NBT_RANGE = "Range"
        private const val NBT_MIN_HARDNESS = "MinHardness"
        private const val NBT_MAX_HARDNESS = "MaxHardness"
        private const val NBT_ABOVE_PLAYER_Y = "AbovePlayerY"

        // Parameter Names
        private const val PARAM_MIN_HARDNESS = "minimum_hardness"  // 最小硬度
        private const val PARAM_MAX_HARDNESS = "maximum_hardness"  // 最大硬度
        private const val PARAM_ABOVE_PLAYER = "only_above_player"  // 仅破坏玩家上方方块
        private const val PARAM_DURATION = "duration"  // 持续时间
        private const val PARAM_RANGE = "break_range"  // 破坏范围

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
        private const val ENHANCEMENT_RANGE = "range"  // 对应破坏范围
    }
}