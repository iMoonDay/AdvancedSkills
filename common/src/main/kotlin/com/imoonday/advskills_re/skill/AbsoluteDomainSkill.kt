package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.block.*
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
        this.settings
            .addParameter("min_hardness_included", 0f)
            .addParameter("max_hardness_excluded", Blocks.OBSIDIAN.hardness)
            .addParameter("above_player_y", true)

        addParameter(
            name = timeParamName,
            baseValue = 3 * 20,
            enhancementId = "time",
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )

        addParameter(
            name = "range",
            baseValue = 1.0,
            enhancementId = "range",
            value = 1.0,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 3
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this, NbtCompound().apply {
        putDouble("Range", getDoubleParam("range", user, 1.0))
        putFloat("MinHardness", getFloatParam("min_hardness_included", user, 0f))
        putFloat("MaxHardness", getFloatParam("max_hardness_excluded", user, Blocks.OBSIDIAN.hardness))
        putBoolean("AbovePlayerY", getBooleanParam("above_player_y", user, true))
    })

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (!player.isUsing()) return
        val world = player.world
        val data = player.getActiveData()
        val range = data.getDouble("Range")
        val hardnessRange = data.getFloat("MinHardness")..<data.getFloat("MaxHardness")
        val abovePlayerY = data.getBoolean("AbovePlayerY")
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
}