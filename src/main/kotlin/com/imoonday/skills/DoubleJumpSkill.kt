package com.imoonday.skills

import com.imoonday.utils.*
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity

class DoubleJumpSkill : Skill(
    id = "double_jump",
    types = arrayOf(SkillType.MOVEMENT),
    cooldown = 3,
    rarity = Rarity.UNCOMMON
) {
    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            jump()
            velocityDirty = true
            velocity = velocity.multiply(1.0, 1.35, 1.0)
            send(EntityVelocityUpdateS2CPacket(this))
            user.spawnParticles(
                ParticleTypes.CLOUD,
                x,
                y,
                z,
                30,
                0.0,
                -velocity.y,
                0.0,
                0.0
            )
        }
        return UseResult.success()
    }
}