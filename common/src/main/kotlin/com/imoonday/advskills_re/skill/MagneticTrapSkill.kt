package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.util.SkillType
import com.imoonday.advskills_re.util.UseResult
import net.minecraft.server.network.*

class MagneticTrapSkill : Skill(
    id = "magnetic_trap",
    types = listOf(SkillType.SUMMON, SkillType.CONTROL),
    cooldown = 8,
    rarity = Rarity.SUPERB
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.world.spawnEntity(MagnetEntity(user.world, user.pos, user))
        return UseResult.success()
    }
}