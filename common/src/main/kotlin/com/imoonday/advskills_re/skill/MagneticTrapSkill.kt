package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.UseResult
import net.minecraft.server.network.*

class MagneticTrapSkill : Skill(
    id = "magnetic_trap",
    types = listOf(SkillType.SUMMON, SkillType.CONTROL),
    cooldown = 8,
    rarity = SkillRarity.SUPERB,
    enhancements = setOf(SkillEnhancements.RANGE, SkillEnhancements.TIME_UP_LIMIT)
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.world.spawnEntity(MagnetEntity(user.world, user.pos, user).apply {
            val range = user.getEnhancementLvl(SkillEnhancements.RANGE)
            if (range > 0) {
                radius *= 1f + range * 0.2f
            }

            val timeUpLimit = user.getEnhancementLvl(SkillEnhancements.TIME_UP_LIMIT)
            if (timeUpLimit > 0) {
                maxAge += timeUpLimit * 20 * 60
            }
        })
        return UseResult.success()
    }
}