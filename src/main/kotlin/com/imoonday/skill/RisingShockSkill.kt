package com.imoonday.skill

import com.imoonday.init.ModSounds
import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.send
import com.imoonday.util.spawnParticles
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import kotlin.math.max

class RisingShockSkill : Skill(
    id = "rising_shock",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 10,
    rarity = Rarity.RARE,
    sound = ModSounds.DASH
), AutoStopTrigger {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.stopFallFlying()
        user.velocityDirty = true
        user.velocity = Vec3d(0.0, max(user.velocity.y, 0.5), 0.0)
        val noGravity = user.hasNoGravity()
        user.setNoGravity(true)
        user.send(EntityVelocityUpdateS2CPacket(user))
        return UseResult.of(user.startUsing {
            it.putBoolean("noGravity", noGravity)
        })
    }

    override fun getPersistTime(): Int = 8

    override fun onStop(player: ServerPlayerEntity) {
        player.getUsingData()?.let {
            player.setNoGravity(it.getBoolean("noGravity"))
        }
        super.onStop(player)
    }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        if (!player.isUsing()) return
        player.velocityDirty = true
        player.velocity = Vec3d(0.0, max(player.velocity.y, 0.5), 0.0)
        player.send(EntityVelocityUpdateS2CPacket(player))
        player.spawnParticles(
            ParticleTypes.CLOUD,
            Vec3d(player.x, player.boundingBox.minY, player.z),
            10,
            0.5,
            0.5,
            0.5,
            0.1
        )
        player.world.getOtherEntities(player, player.boundingBox.expand(1.0)) { it.isLiving && it.isAlive }.forEach {
            it.velocityDirty = true
            it.velocity = it.velocity.withAxis(Direction.Axis.Y, max(it.velocity.y, 0.5))
        }
        super.serverTick(player, usedTime)
    }
}