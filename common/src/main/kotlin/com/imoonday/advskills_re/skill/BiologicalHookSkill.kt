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

    init {
        addParameter(
            name = "hook_life",
            baseValue = 5 * 20,
            enhancementId = "time",
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        user.world.spawnEntity(HookEntity(user.world, user).apply {
            velocity = user.rotationVector
            setPosition(user.eyePos + user.rotationVector * 0.5)
            life = getIntParam("hook_life", user, 5 * 20)
        })
        return UseResult.success()
    }
}