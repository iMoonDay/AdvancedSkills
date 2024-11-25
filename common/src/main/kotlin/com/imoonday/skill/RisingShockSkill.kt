package com.imoonday.skill

import com.imoonday.init.*
import com.imoonday.trigger.*
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.sendToServer
import com.imoonday.util.spawnParticles
import net.minecraft.entity.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*
import kotlin.math.*

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
        user.sendToServer(EntityVelocityUpdateS2CPacket(user))
        return UseResult.of(user.startUsing {
            it.putBoolean("noGravity", noGravity)
        })
    }

    override val persistTime: Int = 8

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
        player.sendToServer(EntityVelocityUpdateS2CPacket(player))
        player.spawnParticles(
            ParticleTypes.CLOUD,
            false,
            Vec3d(player.x, player.boundingBox.minY, player.z),
            10,
            0.5,
            0.5,
            0.5,
            0.1
        )
        player.world.getNonSpectatingEntities(LivingEntity::class.java, player.boundingBox.expand(1.0)).forEach {
            it.velocityDirty = true
            it.velocity = it.velocity.withAxis(Direction.Axis.Y, max(it.velocity.y, 0.5))
            (it as? ServerPlayerEntity)?.sendToServer(EntityVelocityUpdateS2CPacket(it))
        }
        super.serverTick(player, usedTime)
    }
}