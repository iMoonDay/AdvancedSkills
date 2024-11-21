package com.imoonday.skill

import com.imoonday.block.*
import com.imoonday.init.*
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.block.*
import net.minecraft.server.network.*

class FrostTrapSkill : Skill(
    id = "frost_trap",
    types = listOf(SkillType.CONTROL),
    cooldown = 8,
    rarity = Rarity.EPIC,
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        val world = user.world
        val blockPos = user.blockPos
        val state = world.getBlockState(blockPos)
        if (ModBlocks.FROST_TRAP.get().canPlaceAt(state, world, blockPos) && world.setBlockState(
                blockPos,
                ModBlocks.FROST_TRAP.get().defaultState.with(
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