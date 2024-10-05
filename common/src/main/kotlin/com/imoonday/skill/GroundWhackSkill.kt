package com.imoonday.skill

import com.imoonday.trigger.FallTrigger
import com.imoonday.trigger.LandingTrigger
import com.imoonday.trigger.PersistentTrigger
import com.imoonday.util.*
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Vec3d
import kotlin.math.absoluteValue
import kotlin.math.min

class GroundWhackSkill : Skill(
    id = "ground_whack",
    types = listOf(SkillType.ATTACK, SkillType.MOVEMENT),
    cooldown = 8,
    rarity = Rarity.RARE
), LandingTrigger, PersistentTrigger, FallTrigger {

    override fun use(user: ServerPlayerEntity): UseResult {
        if (user.isOnGround) return UseResult.fail(failedMessage())
        user.run {
            velocity = Vec3d(0.0, min(velocity.y, -1.0), 0.0)
            send(EntityVelocityUpdateS2CPacket(this))
            startUsing()
        }
        return UseResult.success()
    }

    override fun onLanding(player: ServerPlayerEntity, height: Float) {
        if (!player.isUsing()) return
        if (height > 0) {
            val newHeight = min(height.toDouble(), 20.0)
            player.world.getOtherEntities(
                player,
                player.boundingBox.expand(newHeight)
            ) { it.isLiving && it.isAlive && !it.isSpectator && (player.y - it.y).absoluteValue <= 1 }
                .forEach {
                    it.damage(player.damageSources.playerAttack(player), min(newHeight / 2, 5.0).toFloat())
                    it.addVelocity(it.pos.subtract(player.pos).normalize().multiply(min(newHeight / 5, 2.0)))
                }
            player.spawnParticles(
                ParticleTypes.CLOUD,
                player.pos,
                (100 * newHeight).toInt(),
                newHeight,
                0.0,
                newHeight,
                0.1
            )
            player.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP)
        }
        player.stopUsing()
    }

    override fun onFall(amount: Int, player: ServerPlayerEntity, fallDistance: Float, damageMultiplier: Float): Int {
        if (!player.isUsing()) return amount
        return if (fallDistance < 10) 0 else amount / 2
    }

    override fun isDangerousTo(player: ServerPlayerEntity): Boolean = player.isUsing()
}