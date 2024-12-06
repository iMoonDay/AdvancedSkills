package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class PiercingSkill : Skill(
    id = "piercing",
    types = listOf(SkillType.MOVEMENT, SkillType.ATTACK),
    cooldown = 15,
    rarity = SkillRarity.SUPERB,
    sound = ModSounds.PIERCING
), AutoStopTrigger, DangerTrigger {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.stopFallFlying()
        user.velocityDirty = true
        user.velocity = user.horizontalRotationVector.normalize().multiply(1.5, 0.0, 1.5)
        val noGravity = user.hasNoGravity()
        user.setNoGravity(true)
        user.sendPacket(EntityVelocityUpdateS2CPacket(user))
        return UseResult.of(user.startUsing {
            it.putDouble("x", user.velocity.x)
            it.putDouble("z", user.velocity.z)
            it.putBoolean("noGravity", noGravity)
        })
    }

    override val persistTime: Int = 8

    override fun onStop(player: ServerPlayerEntity) {
        player.velocityDirty = true
        player.velocity = Vec3d.ZERO
        player.getActiveData().let {
            if (it.contains("noGravity")) {
                player.setNoGravity(it.getBoolean("noGravity"))
            }
        }
        player.sendPacket(EntityVelocityUpdateS2CPacket(player))
        super.onStop(player)
    }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        if (!player.isUsing()) return
        if (player.horizontalCollision) {
            onStop(player)
            player.stopUsing()
            return
        }
        player.getActiveData().let {
            if (it.contains("x") && it.contains("z")) {
                player.velocityDirty = true
                player.velocity = Vec3d(it.getDouble("x"), 0.0, it.getDouble("z"))
                player.sendPacket(EntityVelocityUpdateS2CPacket(player))
            }
        }
        player.world.getNonSpectatingEntities(
            LivingEntity::class.java, player.boundingBox
        ).filterNot { it === player }
            .forEach {
                it.damage(player.damageSources.playerAttack(player), 6.0f)
                it.velocityDirty = true
                it.addVelocity(it.pos.subtract(player.pos).normalize().multiply(1.5).withAxis(Direction.Axis.Y, 1.0))
                (it as? ServerPlayerEntity)?.sendPacket(EntityVelocityUpdateS2CPacket(it))
            }
        super.serverTick(player, usedTime)
    }

    override fun clientTick(player: PlayerEntity, usedTime: Int) {
        super.clientTick(player, usedTime)
        if (!player.isUsing()) return
        val pos = player.pos
        val halfHeight = player.height / 2.0
        val random = player.random
        for (i in 0 until 20) {
            player.world.addParticle(
                DustParticleEffect.DEFAULT,
                pos.x + random.nextDouble() - 0.5, pos.y + halfHeight, pos.z + random.nextDouble() - 0.5,
                0.0, 0.0, 0.0
            )
        }
    }
}