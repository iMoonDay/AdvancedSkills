package com.imoonday.skills

import com.imoonday.init.ModSounds
import com.imoonday.utils.*
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity

class DashSkill : Skill(
    id = "dash",
    types = arrayOf(SkillType.MOVEMENT),
    cooldown = 40,
    rarity = Rarity.UNCOMMON,
    sound = ModSounds.DASH
) {
    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            velocityDirty = true
            velocity = rotationVector.normalize().multiply(1.5)
            send(EntityVelocityUpdateS2CPacket(this))
            user.spawnParticles(
                ParticleTypes.CLOUD,
                x,
                y,
                z,
                10,
                -velocity.x,
                -velocity.y,
                -velocity.z,
                0.0
            )
        }
        return UseResult.success()
    }
}