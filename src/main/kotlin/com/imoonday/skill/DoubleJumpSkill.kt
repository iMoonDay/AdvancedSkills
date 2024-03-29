package com.imoonday.skill

import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.send
import com.imoonday.util.spawnParticles
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity

class DoubleJumpSkill : Skill(
    id = "double_jump",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 3,
    rarity = Rarity.UNCOMMON
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            stopFallFlying()
            jump()
            velocityDirty = true
            velocity = velocity.multiply(1.0, 1.35, 1.0)
            send(EntityVelocityUpdateS2CPacket(this))
            user.spawnParticles(
                ParticleTypes.CLOUD,
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