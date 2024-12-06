package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.UseResult
import com.imoonday.advskills_re.util.sendPacket
import com.imoonday.advskills_re.util.spawnParticles
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class HorizontalDashSkill : Skill(
    id = "horizontal_dash",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 1,
    rarity = SkillRarity.COMMON,
    sound = ModSounds.DASH
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            velocityDirty = true
            stopFallFlying()
            velocity = rotationVector.withAxis(Direction.Axis.Y, velocity.y).normalize().multiply(1.5)
            sendPacket(EntityVelocityUpdateS2CPacket(this))
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