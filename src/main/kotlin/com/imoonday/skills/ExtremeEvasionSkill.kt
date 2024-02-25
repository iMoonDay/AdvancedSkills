package com.imoonday.skills

import com.imoonday.components.isUsingSkill
import com.imoonday.components.startUsingSkill
import com.imoonday.init.ModSounds
import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.PlayerDamageTrigger
import com.imoonday.trigger.VelocitySyncTrigger
import com.imoonday.utils.Skill
import com.imoonday.utils.SkillType
import com.imoonday.utils.UseResult
import com.imoonday.utils.send
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
), AutoStopTrigger, PlayerDamageTrigger, VelocitySyncTrigger {

    override val persistTime: Int = 10
    override val skill: Skill
        get() = this

    override fun use(user: ServerPlayerEntity): UseResult {
        user.run {
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
        return UseResult.of(user.startUsingSkill(skill))
    }

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float = if (!player.isUsingSkill(this)) amount else 0.0f
}