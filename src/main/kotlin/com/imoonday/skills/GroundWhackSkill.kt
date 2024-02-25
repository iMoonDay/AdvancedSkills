package com.imoonday.skills

import com.imoonday.components.isUsingSkill
import com.imoonday.components.startUsingSkill
import com.imoonday.components.stopUsingSkill
import com.imoonday.trigger.LandingTrigger
import com.imoonday.trigger.PersistentTrigger
import com.imoonday.utils.*
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import kotlin.math.absoluteValue
import kotlin.math.min

class GroundWhackSkill : Skill(
    id = "ground_whack",
    types = arrayOf(SkillType.ATTACK, SkillType.MOVEMENT),
    cooldown = 8,
    rarity = Rarity.RARE
), LandingTrigger, PersistentTrigger {
    override fun use(user: ServerPlayerEntity): UseResult {
        if (user.isOnGround) return UseResult.fail(Text.translatable("advancedSkills.skill.ground_whack.failed"))
        user.run {
            velocity = Vec3d(0.0, min(velocity.y, -1.0), 0.0)
            send(EntityVelocityUpdateS2CPacket(this))
            startUsingSkill(this@GroundWhackSkill)
        }
        return UseResult.success()
    }

    override fun onLanding(player: ServerPlayerEntity, height: Float) {
        if (!player.isUsingSkill(this)) return
        if (height > 0) {
            player.world.getOtherEntities(
                player,
                player.boundingBox.expand(height.toDouble())
            ) { it.isLiving && it.isAlive && (player.y - it.y).absoluteValue <= 1 }
                .forEach {
                    it.damage(player.damageSources.playerAttack(player), min(height / 2, 5.0f))
                    it.addVelocity(it.pos.subtract(player.pos).normalize().multiply(min(height / 5, 2.0f).toDouble()))
                }
            player.spawnParticles(
                ParticleTypes.CLOUD,
                player.x,
                player.y,
                player.z,
                50,
                0.0,
                0.0,
                0.0,
                0.0
            )
            player.world.playSound(null, player.blockPos, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS)
        }
        player.stopUsingSkill(this)
    }
}