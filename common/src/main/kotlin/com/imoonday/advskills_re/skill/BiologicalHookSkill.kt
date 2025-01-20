package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.UseResult
import com.imoonday.advskills_re.util.plus
import com.imoonday.advskills_re.util.times
import net.minecraft.server.network.*

class BiologicalHookSkill : Skill(
    Settings(
        id = "biological_hook",
        types = listOf(SkillType.CONTROL),
        cooldown = 15,
        rarity = SkillRarity.SUPERB
    )
) {

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings.addParameter(
            name = PARAM_DURATION,
            baseValue = DEFAULT_DURATION,
            enhancementId = ENHANCEMENT_DURATION,
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        user.world.spawnEntity(HookEntity(user.world, user).apply {
            velocity = user.rotationVector
            setPosition(user.eyePos + user.rotationVector * 0.5)
            life = getIntParam(PARAM_DURATION, user, DEFAULT_DURATION)
        })
        return UseResult.success()
    }

    companion object {

        // Default Values
        private const val DEFAULT_DURATION = 5 * 20

        // Parameter Names
        private const val PARAM_DURATION = "hook_duration"  // 钩子持续时间

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
    }
}