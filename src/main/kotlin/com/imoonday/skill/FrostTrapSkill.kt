package com.imoonday.skill

import com.imoonday.block.FrostTrapBlock
import com.imoonday.init.ModBlocks
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.block.SnowBlock
import net.minecraft.server.network.ServerPlayerEntity

class FrostTrapSkill : Skill(
    id = "frost_trap",
    types = listOf(SkillType.CONTROL),
    cooldown = 15,
    rarity = Rarity.EPIC,
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        val world = user.world
        val blockPos = user.blockPos
        val state = world.getBlockState(blockPos)
        if (ModBlocks.FROST_TRAP.canPlaceAt(state, world, blockPos) && world.setBlockState(
                blockPos,
                ModBlocks.FROST_TRAP.defaultState.with(
                    SnowBlock.LAYERS,
                    if (state.contains(SnowBlock.LAYERS)) (state.get(SnowBlock.LAYERS) + 1).coerceAtMost(8) else 1
                )
            )
        ) {
            (world.getBlockState(blockPos).block as? FrostTrapBlock)?.updatePlacer(
                world,
                blockPos,
                user
            )
            return UseResult.success()
        }
        return UseResult.fail(failedMessage())
    }
}