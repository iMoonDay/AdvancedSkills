package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.effect.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class SuperShadowCloneSkill : Skill(
    id = "super_shadow_clone",
    types = listOf(SkillType.SUMMON),
    cooldown = 90,
    rarity = SkillRarity.LEGENDARY,
), SendPlayerVelocityTrigger {

    override fun use(user: ServerPlayerEntity): UseResult {
        spawnClones(user, 8)
        user.addStatusEffect(StatusEffectInstance(StatusEffects.INVISIBILITY, 5 * 20, 0, true, false, true))
        return UseResult.success()
    }

    private fun spawnClones(player: ServerPlayerEntity, amount: Int) {
        val fullCircle = 360f
        val angleStep = fullCircle / amount
        val yaw = player.yaw
        for (i in 0 until amount) {
            val angle = i * angleStep + yaw
            val adjustedAngle = when {
                angle >= 180 -> angle - fullCircle
                angle <= -180 -> angle + fullCircle
                else -> angle
            }
            player.world.spawnEntity(
                createCloneEntity(
                    player,
                    player.getRotationVector(0f, adjustedAngle)
                ).apply {
                    this.yaw = adjustedAngle
                    headYaw = adjustedAngle
                })
        }
    }

    private fun createCloneEntity(player: ServerPlayerEntity, horizontalRotation: Vec3d): ClonePlayerEntity {
        return ClonePlayerEntity(player.world, player).apply {
            moveVelocity = horizontalRotation * (player.velocity.length() * 2.0).coerceAtMost(1.0)
            moveTime = 5 * 20
            if (player.velocity.y > 0) {
                jumpControl.setActive()
                setJumping(true)
            }
        }
    }
}