package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.block.Blocks.*
import net.minecraft.entity.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class WeedCleanerSkill : Skill(
    id = "weed_cleaner",
    types = listOf(SkillType.UTILITY),
    cooldown = 10,
    rarity = SkillRarity.COMMON,
    enhancements = setOf(SkillEnhancements.RANGE)
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        val world = user.world
        val userPos = user.pos
        val range = 25.0 + user.getEnhancementLvl(SkillEnhancements.RANGE) * 5.0
        user.boundingBox.expand(range).blockPosSet.forEach { pos ->
            val state = world.getBlockState(pos)
            if (WEEDS.contains(state.block)) {
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

        val WEEDS = listOf(
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
        )
    }
}