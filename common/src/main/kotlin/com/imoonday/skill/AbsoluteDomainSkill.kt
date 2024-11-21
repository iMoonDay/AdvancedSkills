package com.imoonday.skill

import com.imoonday.trigger.*
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.blockPosSet
import com.imoonday.util.spawnParticles
import net.minecraft.block.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class AbsoluteDomainSkill : Skill(
    id = "absolute_domain",
    types = listOf(SkillType.DESTRUCTION),
    cooldown = 15,
    rarity = Rarity.RARE,
), AutoStopTrigger {

    private val maxHardness = Blocks.OBSIDIAN.hardness

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun getPersistTime(): Int = 20 * 3

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (!player.isUsing()) return
        player.boundingBox.expand(1.0).blockPosSet.filter {
            val hardness = player.world.getBlockState(it).getHardness(player.world, it)
            hardness < maxHardness && hardness >= 0 && it.y >= player.blockY
        }.forEach {
            val centerPos = it.toCenterPos()
            player.spawnParticles(ParticleTypes.SMOKE, centerPos, 1, 0.0, 0.0, 0.0, 0.0)
            player.world.breakBlock(it, true, player)
        }
    }
}