package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.util.SkillType
import com.imoonday.advskills_re.util.UseResult
import com.imoonday.advskills_re.util.sendToServer
import com.imoonday.advskills_re.util.spawnParticles
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

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
            sendToServer(EntityVelocityUpdateS2CPacket(this))
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