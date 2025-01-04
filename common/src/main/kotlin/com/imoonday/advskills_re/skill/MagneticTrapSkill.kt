package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.UseResult
import net.minecraft.server.network.*

class MagneticTrapSkill : Skill(
    Settings(
        id = "magnetic_trap",
        types = listOf(SkillType.SUMMON, SkillType.CONTROL),
        cooldown = 8,
        rarity = SkillRarity.SUPERB
    )
) {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(
                name = PARAM_ATTRACT_RANGE,
                baseValue = DEFAULT_ATTRACT_RANGE,
                enhancementId = ENHANCEMENT_RANGE,
                value = 0.2f,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_TRAP_DURATION,
                baseValue = DEFAULT_TRAP_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 20 * 60,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        user.world.spawnEntity(MagnetEntity(user.world, user.pos, user).apply {
            radius *= getFloatParam(PARAM_ATTRACT_RANGE, user, DEFAULT_ATTRACT_RANGE)
            maxAge += getIntParam(PARAM_TRAP_DURATION, user, DEFAULT_TRAP_DURATION)
        })
        return UseResult.success()
    }

    companion object {
        // Default Values
        private const val DEFAULT_ATTRACT_RANGE = 1.0f
        private const val DEFAULT_TRAP_DURATION = 0

        // Parameter Names
        private const val PARAM_ATTRACT_RANGE = "attract_range"  // 吸引范围
        private const val PARAM_TRAP_DURATION = "trap_duration"  // 陷阱持续时间

        // Enhancement IDs
        private const val ENHANCEMENT_RANGE = "range"  // 对应吸引范围
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
    }
}