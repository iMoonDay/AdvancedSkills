package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class PiercingSkill : Skill(
    id = "piercing",
    types = listOf(SkillType.MOVEMENT, SkillType.ATTACK),
    cooldown = 15,
    rarity = SkillRarity.SUPERB,
    sound = ModSounds.PIERCING,
    enhancements = setOf(SkillEnhancements.PERSISTENT_TIME, SkillEnhancements.DAMAGE, SkillEnhancements.VELOCITY)
), AutoStopTrigger, DangerTrigger, GravityTrigger {

    override fun use(user: ServerPlayerEntity): UseResult {
        user.stopFallFlying()
        user.velocity = user.horizontalRotationVector.normalize().multiply(1.5, 0.0, 1.5)
        user.updateVelocity()
        return UseResult.of(user.startUsing {
            it.putDouble("x", user.velocity.x)
            it.putDouble("z", user.velocity.z)
        })
    }

    override val persistTime: Int = 8

    override fun onStop(player: ServerPlayerEntity) {
        player.velocity = Vec3d.ZERO
        player.updateVelocity()
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
                player.velocity = Vec3d(it.getDouble("x"), 0.0, it.getDouble("z"))
                player.updateVelocity()
            }
        }
        val damage = getEnhancedValue(player, SkillEnhancements.DAMAGE, 6.0f)
        val velocity = 1.5 + player.getEnhancementLvl(SkillEnhancements.VELOCITY) * 0.1
        player.world.getOtherEntities(
            player, player.boundingBox
        ) { it is LivingEntity }.forEach {
            it.damage(player.damageSources.playerAttack(player), damage)
            it.addVelocity(it.pos.subtract(player.pos).normalize().multiply(velocity).withAxis(Direction.Axis.Y, 1.0))
            it.velocityDirty = true
            (it as? ServerPlayerEntity)?.updateVelocity()
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