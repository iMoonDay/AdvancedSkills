package com.imoonday.skill

import com.imoonday.init.ModSounds
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.send
import com.imoonday.util.spawnParticles
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity

class DashSkill : Skill(
    id = "dash",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 2,
    rarity = Rarity.UNCOMMON,
    sound = ModSounds.DASH
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            velocityDirty = true
            stopFallFlying()
            velocity = rotationVector.normalize().multiply(1.5)
            send(EntityVelocityUpdateS2CPacket(this))
            spawnParticles(
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