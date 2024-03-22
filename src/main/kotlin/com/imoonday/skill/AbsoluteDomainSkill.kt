package com.imoonday.skill

import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.blockPosSet
import com.imoonday.util.spawnParticles
import net.minecraft.block.Blocks
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity

class AbsoluteDomainSkill : Skill(
    id = "absolute_domain",
    types = listOf(SkillType.DESTRUCTION),
    cooldown = 10,
    rarity = Rarity.RARE,
) {

    private val maxHardness = Blocks.OBSIDIAN.hardness

    override fun use(user: ServerPlayerEntity): UseResult {
        user.boundingBox.expand(1.0).blockPosSet.filter {
            val hardness = user.world.getBlockState(it).getHardness(user.world, it)
            hardness < maxHardness && hardness >= 0 && it.y >= user.blockY
        }.forEach {
            val centerPos = it.toCenterPos()
            user.spawnParticles(ParticleTypes.SMOKE, centerPos, 1, 0.0, 0.0, 0.0, 0.0)
            user.world.breakBlock(it, true, user)
        }
        return UseResult.success()
    }
}