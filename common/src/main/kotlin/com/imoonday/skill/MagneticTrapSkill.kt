package com.imoonday.skill

import com.imoonday.entity.MagnetEntity
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.server.network.ServerPlayerEntity

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