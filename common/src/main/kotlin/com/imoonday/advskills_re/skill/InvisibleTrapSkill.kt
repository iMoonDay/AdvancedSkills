package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.block.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.util.SkillType
import com.imoonday.advskills_re.util.UseResult
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