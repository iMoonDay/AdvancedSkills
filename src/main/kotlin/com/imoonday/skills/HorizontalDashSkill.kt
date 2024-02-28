package com.imoonday.skills

import com.imoonday.init.ModSounds
import com.imoonday.utils.*
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Direction

class HorizontalDashSkill : Skill(
    id = "horizontal_dash",
    types = arrayOf(SkillType.MOVEMENT),
    cooldown = 1,
    rarity = Rarity.COMMON,
    sound = ModSounds.DASH
) {
    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            velocityDirty = true
            stopFallFlying()
            velocity = rotationVector.withAxis(Direction.Axis.Y, velocity.y).normalize().multiply(1.5)
            send(EntityVelocityUpdateS2CPacket(this))
            user.spawnParticles(
                ParticleTypes.CLOUD,
                x,
                y,
                z,
                10,
                -velocity.x,
                0.0,
                -velocity.z,
                0.0
            )
        }
        return UseResult.success()
    }
}