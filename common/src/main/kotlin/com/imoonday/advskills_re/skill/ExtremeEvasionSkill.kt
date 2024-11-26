package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.SkillType
import com.imoonday.advskills_re.util.UseResult
import com.imoonday.advskills_re.util.sendToServer
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.server.world.*

class ExtremeEvasionSkill : Skill(
    id = "extreme_evasion",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 10,
    rarity = Rarity.EPIC,
    sound = ModSounds.DASH
), AutoStopTrigger, DamageTrigger, SendPlayerVelocityTrigger {

    override val persistTime: Int = 10

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            stopFallFlying()
            velocity = (if (velocity.x == 0.0 && velocity.z == 0.0) rotationVector else velocity).normalize()
                .multiply(2.0, 0.0, 2.0)
            sendToServer(EntityVelocityUpdateS2CPacket(this))
            (world as ServerWorld).spawnParticles(
                ParticleTypes.CLOUD,
                x,
                y,
                z,
                10,
                -velocity.x,
                0.0,
                -velocity.z,
                0.0
            )
        }
        return UseResult.startUsing(user, this)
    }

    override fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?,
    ): Boolean = player.isUsing()
}