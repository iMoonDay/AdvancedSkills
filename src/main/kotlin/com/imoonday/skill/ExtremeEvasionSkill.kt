package com.imoonday.skill

import com.imoonday.init.ModSounds
import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.DamageTrigger
import com.imoonday.trigger.SendPlayerVelocityTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.send
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld

class ExtremeEvasionSkill : Skill(
    id = "extreme_evasion",
    types = arrayOf(SkillType.MOVEMENT),
    cooldown = 10,
    rarity = Rarity.EPIC,
    sound = ModSounds.DASH
), AutoStopTrigger, DamageTrigger, SendPlayerVelocityTrigger {

    override fun getPersistTime(): Int = 10

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
            stopFallFlying()
            velocity = (if (velocity.x == 0.0 && velocity.z == 0.0) rotationVector else velocity).normalize()
                .multiply(2.0, 0.0, 2.0)
            send(EntityVelocityUpdateS2CPacket(this))
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

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float = if (!player.isUsing()) amount else 0.0f
}