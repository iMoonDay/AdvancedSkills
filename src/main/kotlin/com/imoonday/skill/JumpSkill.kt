package com.imoonday.skill

import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.send
import com.imoonday.util.spawnParticles
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity

class JumpSkill : Skill(
    id = "jump",
    types = arrayOf(SkillType.MOVEMENT),
    cooldown = 1,
    rarity = Rarity.COMMON
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            stopFallFlying()
            jump()
            send(EntityVelocityUpdateS2CPacket(this))
            user.spawnParticles(
                ParticleTypes.CLOUD,
                x, y, z,
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