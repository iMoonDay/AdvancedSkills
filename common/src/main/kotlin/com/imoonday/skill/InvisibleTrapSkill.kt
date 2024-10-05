package com.imoonday.skill

import com.imoonday.block.InvisibleTrapBlock
import com.imoonday.init.ModBlocks
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.fluid.Fluids
import net.minecraft.server.network.ServerPlayerEntity

class InvisibleTrapSkill : Skill(
    id = "invisible_trap",
    types = listOf(SkillType.ATTACK),
    cooldown = 8,
    rarity = Rarity.UNCOMMON,
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        val world = user.world
        val blockPos = user.blockPos
        if (ModBlocks.INVISIBLE_TRAP.canPlaceAt(world.getBlockState(blockPos), world, blockPos) && world.setBlockState(
                blockPos,
                ModBlocks.INVISIBLE_TRAP.defaultState.with(
                    InvisibleTrapBlock.WATERLOGGED,
                    world.getFluidState(blockPos).fluid == Fluids.WATER
                )
            )
        ) {
            (world.getBlockState(blockPos).block as? InvisibleTrapBlock)?.updatePlacer(
                world,
                blockPos,
                user
            )
            return UseResult.success()
        }
        return UseResult.fail(failedMessage())
    }
}