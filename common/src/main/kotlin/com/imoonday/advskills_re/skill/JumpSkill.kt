package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.util.SkillType
import com.imoonday.advskills_re.util.UseResult
import com.imoonday.advskills_re.util.sendToServer
import com.imoonday.advskills_re.util.spawnParticles
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class JumpSkill : Skill(
    id = "jump",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 1,
    rarity = Rarity.COMMON
) {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            stopFallFlying()
            jump()
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