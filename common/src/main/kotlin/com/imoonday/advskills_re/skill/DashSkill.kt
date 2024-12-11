package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class DashSkill : Skill(
    id = "dash",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 2,
    rarity = SkillRarity.UNCOMMON,
    sound = ModSounds.DASH,
    enhancements = setOf(SkillEnhancements.VELOCITY)
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            stopFallFlying()
            val multiplier = user.getEnhancementLvl(SkillEnhancements.VELOCITY) * 0.1
            velocity = rotationVector.normalize().multiply(1.5 + multiplier)
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