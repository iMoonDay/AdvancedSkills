package com.imoonday.skill

import com.imoonday.init.ModSounds
import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.getUsingData
import com.imoonday.util.send
import net.minecraft.entity.LivingEntity
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d

class PiercingSkill : Skill(
    id = "piercing",
    types = listOf(SkillType.MOVEMENT, SkillType.ATTACK),
    cooldown = 15,
    rarity = Rarity.SUPERB,
    sound = ModSounds.PIERCING
), AutoStopTrigger {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.stopFallFlying()
        user.velocityDirty = true
        user.velocity = user.rotationVector.normalize().multiply(1.5, 0.0, 1.5)
        val noGravity = user.hasNoGravity()
        user.setNoGravity(true)
        user.send(EntityVelocityUpdateS2CPacket(user))
        return UseResult.of(user.startUsing {
            it.putDouble("x", user.velocity.x)
            it.putDouble("z", user.velocity.z)
            it.putBoolean("noGravity", noGravity)
        })
    }

    override fun getPersistTime(): Int = 8

    override fun onStop(player: ServerPlayerEntity) {
        player.velocityDirty = true
        player.velocity = Vec3d.ZERO
        player.getUsingData(this)?.let {
            player.setNoGravity(it.getBoolean("noGravity"))
        }
        player.send(EntityVelocityUpdateS2CPacket(player))
        super.onStop(player)
    }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        if (!player.isUsing()) return
        if (player.horizontalCollision) {
            onStop(player)
            player.stopUsing()
            return
        }
        player.getUsingData(this)?.let {
            if (it.contains("x") && it.contains("z")) {
                player.velocityDirty = true
                player.velocity = Vec3d(it.getDouble("x"), 0.0, it.getDouble("z"))
                player.send(EntityVelocityUpdateS2CPacket(player))
            }
        }
        player.world.getNonSpectatingEntities(
            LivingEntity::class.java, player.boundingBox
        ).forEach {
            it.damage(player.damageSources.playerAttack(player), 6.0f)
            it.velocityDirty = true
            it.addVelocity(it.pos.subtract(player.pos).normalize().multiply(1.5).withAxis(Direction.Axis.Y, 1.0))
            (it as? ServerPlayerEntity)?.send(EntityVelocityUpdateS2CPacket(it))
        }
        super.serverTick(player, usedTime)
    }

    override fun isDangerousTo(player: ServerPlayerEntity): Boolean = player.isUsing()
}