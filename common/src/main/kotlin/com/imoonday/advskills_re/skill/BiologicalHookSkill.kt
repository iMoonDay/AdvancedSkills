package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.UseResult
import com.imoonday.advskills_re.util.plus
import com.imoonday.advskills_re.util.times
import net.minecraft.server.network.*

class BiologicalHookSkill : Skill(
    id = "biological_hook",
    types = listOf(SkillType.CONTROL),
    cooldown = 15,
    rarity = SkillRarity.SUPERB,
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.world.spawnEntity(HookEntity(user.world, user).apply {
            velocity = user.rotationVector
            setPosition(user.eyePos + user.rotationVector * 0.5)
        })
        return UseResult.success()
    }
}