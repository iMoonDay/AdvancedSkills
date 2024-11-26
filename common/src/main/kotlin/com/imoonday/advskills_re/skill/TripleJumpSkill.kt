package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.util.SkillType
import com.imoonday.advskills_re.util.UseResult
import com.imoonday.advskills_re.util.sendToServer
import com.imoonday.advskills_re.util.spawnParticles
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class TripleJumpSkill : Skill(
    id = "triple_jump",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 5,
    rarity = Rarity.RARE
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            stopFallFlying()
            jump()
            velocityDirty = true
            velocity = velocity.multiply(1.0, 1.7, 1.0)
            sendToServer(EntityVelocityUpdateS2CPacket(this))
            user.spawnParticles(
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