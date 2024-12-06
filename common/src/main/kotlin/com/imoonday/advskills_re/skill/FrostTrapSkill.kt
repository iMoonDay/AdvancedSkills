package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.block.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.UseResult
import net.minecraft.block.*
import net.minecraft.server.network.*

class FrostTrapSkill : Skill(
    id = "frost_trap",
    types = listOf(SkillType.CONTROL),
    cooldown = 8,
    rarity = SkillRarity.EPIC,
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