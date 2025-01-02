package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class DoubleJumpSkill : Skill(
    Settings(
        id = "double_jump",
        types = listOf(SkillType.MOVEMENT),
        cooldown = 3,
        rarity = SkillRarity.UNCOMMON
    )
) {

    init {
        addParameter(
            name = "jump_power",
            baseValue = 1.35,
            enhancementId = "power",
            value = 0.1,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            stopFallFlying()
            jump()
            val power = getDoubleParam("jump_power", user, 1.35)
            velocity = velocity.multiply(1.0, power, 1.0)
            updateVelocity()
            user.spawnParticles(ParticleTypes.CLOUD, false, pos, 10, 0.5, 0.0, 0.5, 0.1)
        }
        return UseResult.success()
    }
}