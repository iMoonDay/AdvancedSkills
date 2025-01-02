package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.block.Blocks.*
import net.minecraft.entity.*
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

    override fun initDefaultSettings(settings: Settings) {
        settings.addParameter(
            name = "clean_range",
            baseValue = 25.0,
            enhancementId = "range",
            value = 5.0,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.FLOAT
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        val world = user.world
        val userPos = user.pos
        val range = getDoubleParam("clean_range", user, 25.0)
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