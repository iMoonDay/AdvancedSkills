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

    private val maxHardness = Blocks.OBSIDIAN.hardness

    init {
        addEnhanceableParameter(
            name = timeParamName,
            baseValue = 3 * 20,
            enhancementId = "time",
            value = 0.2f,
            operation = Enhancement.Operation.MULTIPLY,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )
        addEnhanceableParameter(
            name = "range",
            baseValue = 1.0,
            enhancementId = "range",
            value = 1f,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 3,
            descArg = Enhancement.ArgFormatters.SELF
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this, NbtCompound().apply {
        putDouble("Range", user.getDoubleParam("range"))
    })

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (!player.isUsing()) return
        val range = player.getActiveData().getDouble("Range")
        player.boundingBox.expand(1.0 + range).blockPosSet.filter {
            val hardness = player.world.getBlockState(it).getHardness(player.world, it)
            hardness < maxHardness && hardness >= 0 && it.y >= player.blockY
        }.forEach {
            val centerPos = it.toCenterPos()
            player.spawnParticles(ParticleTypes.SMOKE, false, centerPos, 1, 0.0, 0.0, 0.0, 0.0)
            player.world.breakBlock(it, true, player)
        }
    }

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }
}