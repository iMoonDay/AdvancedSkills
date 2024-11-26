package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.SkillType
import com.imoonday.advskills_re.util.UseResult
import com.imoonday.advskills_re.util.getUsingData
import com.imoonday.advskills_re.util.sendToServer
import net.minecraft.entity.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

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
        user.sendToServer(EntityVelocityUpdateS2CPacket(user))
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
        player.getUsingData()?.let {
            player.setNoGravity(it.getBoolean("noGravity"))
        }
        player.sendToServer(EntityVelocityUpdateS2CPacket(player))
        super.onStop(player)
    }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        if (!player.isUsing()) return
        if (player.horizontalCollision) {
            onStop(player)
            player.stopUsing()
            return
        }
        player.getUsingData()?.let {
            if (it.contains("x") && it.contains("z")) {
                player.velocityDirty = true
                player.velocity = Vec3d(it.getDouble("x"), 0.0, it.getDouble("z"))
                player.sendToServer(EntityVelocityUpdateS2CPacket(player))
            }
        }
        player.world.getNonSpectatingEntities(
            LivingEntity::class.java, player.boundingBox
        ).forEach {
            if (it === player) return@forEach
            it.damage(player.damageSources.playerAttack(player), 6.0f)
            it.velocityDirty = true
            it.addVelocity(it.pos.subtract(player.pos).normalize().multiply(1.5).withAxis(Direction.Axis.Y, 1.0))
            (it as? ServerPlayerEntity)?.sendToServer(EntityVelocityUpdateS2CPacket(it))
        }
        super.serverTick(player, usedTime)
    }

    override fun isDangerous(player: ServerPlayerEntity): Boolean = player.isUsing()
}