package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.block.Blocks.*
import net.minecraft.entity.*
import net.minecraft.registry.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class WeedCleanerSkill : Skill(
    Settings(
        id = "weed_cleaner",
        types = listOf(SkillType.UTILITY),
        cooldown = 10,
        rarity = SkillRarity.COMMON
    )
) {

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings
            .addParameter(PARAM_WEED_BLOCKS, WEED_BLOCKS)
            .addParameter(
                name = PARAM_CLEAN_RANGE,
                baseValue = DEFAULT_CLEAN_RANGE,
                enhancementId = ENHANCEMENT_RANGE,
                value = 5.0,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.FLOAT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        val world = user.world
        val userPos = user.pos
        val cleanRange = getDoubleParam(PARAM_CLEAN_RANGE, user, DEFAULT_CLEAN_RANGE)
        val weedBlocks = getListParam(PARAM_WEED_BLOCKS).getStringList()
        user.boundingBox.expand(cleanRange).blockPosSet.forEach { pos ->
            val state = world.getBlockState(pos)
            val id = Registries.BLOCK.getId(state.block).toString()
            if (weedBlocks.contains(id)) {
                if (world.breakBlock(pos, true, user)) {
                    world.getEntitiesByClass(ItemEntity::class.java, Box(pos).expand(1.0)) { it.age == 0 }
                        .forEach {
                            it.resetPickupDelay()
                            it.setPosition(userPos)
                        }
                }
            }
        }
        return UseResult.success()
    }

    companion object {

        // Default Values
        private const val DEFAULT_CLEAN_RANGE = 25.0

        // Parameter Names
        private const val PARAM_CLEAN_RANGE = "clean_range"  // 清理范围
        private const val PARAM_WEED_BLOCKS = "weed_blocks"  // 杂草方块列表

        // Enhancement IDs
        private const val ENHANCEMENT_RANGE = "range"  // 对应范围

        // Block Lists
        val WEED_BLOCKS = listOf(  // 杂草方块列表
            GRASS,
            TALL_GRASS,
            FERN,
            LARGE_FERN,
            SEAGRASS,
            TALL_SEAGRASS,
            DEAD_BUSH,
            VINE,
            CRIMSON_ROOTS,
            WARPED_ROOTS,
            NETHER_SPROUTS,
            WEEPING_VINES,
            TWISTING_VINES
        ).map { Registries.BLOCK.getId(it).toString() }
    }
}