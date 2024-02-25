package com.imoonday.skills

import com.imoonday.utils.*
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity

class TripleJumpSkill : Skill(
    id = "triple_jump",
    types = arrayOf(SkillType.MOVEMENT),
    cooldown = 5,
    rarity = Rarity.RARE
) {
    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            jump()
            velocityDirty = true
            velocity = velocity.multiply(1.0, 1.7, 1.0)
            send(EntityVelocityUpdateS2CPacket(this))
            user.spawnParticles(
                ParticleTypes.CLOUD,
                x,
                y,
                z,
                100,
                0.0,
                -velocity.y,
                0.0,
                0.0
            )
        }
        return UseResult.success()
    }
}