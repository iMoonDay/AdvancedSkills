package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class DoubleJumpSkill : Skill(
    id = "double_jump",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 3,
    rarity = SkillRarity.UNCOMMON,
    enhancements = setOf(SkillEnhancements.POWER)
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            stopFallFlying()
            jump()
            val power = 1.35 * (1 + user.getEnhancementLvl(SkillEnhancements.POWER) * 0.1)
            velocity = velocity.multiply(1.0, power, 1.0)
            updateVelocity()
            user.spawnParticles(
                ParticleTypes.CLOUD,
                false,
                pos,
                10,
                0.5,
                0.0,
                0.5,
                0.1
            )
        }
        return UseResult.success()
    }
}