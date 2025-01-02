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
                name = "radius_multiplier",
                baseValue = 1.0f,
                enhancementId = "range",
                value = 0.2f,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "additional_age",
                baseValue = 0,
                enhancementId = "time",
                value = 20 * 60,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        user.world.spawnEntity(MagnetEntity(user.world, user.pos, user).apply {
            radius *= getFloatParam("radius_multiplier", user, 1.0f)
            maxAge += getIntParam("additional_age", user, 0)
        })
        return UseResult.success()
    }
}