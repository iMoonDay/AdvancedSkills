package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.UseResult
import net.minecraft.server.network.*
import net.minecraft.sound.*
import kotlin.math.*

class UndeadSummoningSkill : Skill(
    Settings(
        id = "undead_summoning",
        types = listOf(SkillType.SUMMON),
        cooldown = 30,
        rarity = SkillRarity.EPIC
    )
) {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(PARAM_SUMMON_SOUND, DEFAULT_SUMMON_SOUND)
            .addParameter(
                name = PARAM_TOTAL_COUNT,
                baseValue = DEFAULT_TOTAL_COUNT,
                enhancementId = ENHANCEMENT_COUNT,
                value = 2,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        val totalCount = getIntParam(PARAM_TOTAL_COUNT, user, DEFAULT_TOTAL_COUNT)
        val maxSkeletonCount = floor(totalCount / 2.0).toInt()
        val skeletonCount = (1..maxSkeletonCount).random()
        
        repeat(skeletonCount) {
            user.world.spawnEntity(ServantSkeletonEntity(user.world, user))
        }
        repeat((1..min(totalCount - skeletonCount, maxSkeletonCount)).random()) {
            user.world.spawnEntity(ServantWitherSkeletonEntity(user.world, user))
        }
        
        return UseResult.success(sound = getSoundEventParam(PARAM_SUMMON_SOUND, DEFAULT_SUMMON_SOUND))
    }

    companion object {
        // Default Values
        private const val DEFAULT_TOTAL_COUNT = 5
        private val DEFAULT_SUMMON_SOUND = SoundEvents.ENTITY_WITHER_SPAWN

        // Parameter Names
        private const val PARAM_SUMMON_SOUND = "summon_sound"  // 召唤音效
        private const val PARAM_TOTAL_COUNT = "total_count"  // 总召唤数量

        // Enhancement IDs
        private const val ENHANCEMENT_COUNT = "count"  // 对应数量
    }
}