package com.imoonday.skills

import com.imoonday.utils.Skill
import com.imoonday.utils.SkillType
import com.imoonday.utils.UseResult
import com.imoonday.utils.blockPosSet
import net.minecraft.block.Blocks
import net.minecraft.server.network.ServerPlayerEntity

class AbsoluteDomainSkill : Skill(
    id = "absolute_domain",
    types = arrayOf(SkillType.DESTRUCTION),
    cooldown = 10,
    rarity = Rarity.RARE,
) {
    private val maxHardness = Blocks.OBSIDIAN.hardness

    override fun use(user: ServerPlayerEntity): UseResult {
        user.boundingBox.expand(1.0).blockPosSet.filter {
            val hardness = user.world.getBlockState(it).getHardness(user.world, it)
            hardness < maxHardness && hardness >= 0 && it.y >= user.blockY
        }.forEach {
            user.world.breakBlock(it, true, user)
        }
        return UseResult.success()
    }
}