package com.imoonday.skill

import com.imoonday.init.*
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.sendToServer
import com.imoonday.util.spawnParticles
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class HorizontalDashSkill : Skill(
    id = "horizontal_dash",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 1,
    rarity = Rarity.COMMON,
    sound = ModSounds.DASH
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            velocityDirty = true
            stopFallFlying()
            velocity = rotationVector.withAxis(Direction.Axis.Y, velocity.y).normalize().multiply(1.5)
            sendToServer(EntityVelocityUpdateS2CPacket(this))
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