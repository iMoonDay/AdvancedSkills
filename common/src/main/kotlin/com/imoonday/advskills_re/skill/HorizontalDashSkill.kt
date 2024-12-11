package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class HorizontalDashSkill : Skill(
    id = "horizontal_dash",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 1,
    rarity = SkillRarity.COMMON,
    sound = ModSounds.DASH,
    enhancements = setOf(SkillEnhancements.VELOCITY),
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            stopFallFlying()
            val extraVelocity = user.getEnhancementLvl(SkillEnhancements.VELOCITY) * 0.1
            velocity = rotationVector.withAxis(Direction.Axis.Y, velocity.y).normalize().multiply(1.5 + extraVelocity)
            updateVelocity()
            spawnParticles(
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