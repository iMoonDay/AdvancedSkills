package com.imoonday.skill

import com.imoonday.block.*
import com.imoonday.init.*
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.fluid.*
import net.minecraft.server.network.*

class InvisibleTrapSkill : Skill(
    id = "invisible_trap",
    types = listOf(SkillType.ATTACK),
    cooldown = 8,
    rarity = Rarity.UNCOMMON,
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        val world = user.world
        val blockPos = user.blockPos
        if (ModBlocks.INVISIBLE_TRAP.get().canPlaceAt(world.getBlockState(blockPos), world, blockPos) && world.setBlockState(
                blockPos,
                ModBlocks.INVISIBLE_TRAP.get().defaultState.with(
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