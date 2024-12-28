package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.block.*
import com.imoonday.advskills_re.block.entity.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.UseResult
import net.minecraft.fluid.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class InvisibleTrapSkill : Skill(
    id = "invisible_trap",
    types = listOf(SkillType.ATTACK),
    cooldown = 8,
    rarity = SkillRarity.UNCOMMON,
    enhancements = setOf(SkillEnhancements.RANGE, SkillEnhancements.EFFECT_COUNT, SkillEnhancements.DAMAGE)
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        val world = user.world
        val pos = user.blockPos
        val range = user.getEnhancementLvl(SkillEnhancements.RANGE)
        val times = 1 + user.getEnhancementLvl(SkillEnhancements.EFFECT_COUNT)
        var success = false
        val trapBlock = ModBlocks.INVISIBLE_TRAP.get()
        val defaultState = trapBlock.defaultState
        val modifyDamage: (Float) -> Float = { getEnhancedValue(user, SkillEnhancements.DAMAGE, it) }
        val uuid = user.uuid

        BlockPos.iterateOutwards(pos, range, 0, range).forEach {
            val wasTrap = world.getBlockEntity(it) is InvisibleTrapBlockEntity
            for (i in 0 until times) {
                val state = world.getBlockState(it)
                val canPlace = (state.isAir || state.isOf(trapBlock)) && trapBlock.canPlaceAt(state, world, it)
                if (!canPlace) continue

                val hasFluid = world.getFluidState(it).fluid == Fluids.WATER
                val newState = defaultState.with(InvisibleTrapBlock.WATERLOGGED, hasFluid)

                world.setBlockState(it, newState)
            }

            val blockEntity = world.getBlockEntity(it)
            if (blockEntity is InvisibleTrapBlockEntity) {
                blockEntity.placer = uuid
                if (!wasTrap) {
                    blockEntity.damage = modifyDamage(blockEntity.damage)
                }
                blockEntity.markDirty()
                success = true
            }
        }

        return if (success) UseResult.success() else UseResult.fail(failedMessage())
    }
}