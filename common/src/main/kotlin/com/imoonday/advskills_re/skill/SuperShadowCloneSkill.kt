package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.effect.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class SuperShadowCloneSkill : Skill(
    Settings(
        id = "super_shadow_clone",
        types = listOf(SkillType.SUMMON),
        cooldown = 90,
        rarity = SkillRarity.LEGENDARY
    )
), SendPlayerVelocityTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter("move_time", 5 * 20)
            .addParameter(
                name = "clone_amount",
                baseValue = 8,
                enhancementId = "amount",
                value = 2,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = "invisibility_duration",
                baseValue = 5 * 20,
                enhancementId = "duration",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        val amount = getIntParam("clone_amount", user, 8)
        val moveTime = getIntParam("move_time", user, 5 * 20)
        spawnClones(user, amount, moveTime)
        val duration = getIntParam("invisibility_duration", user, 5 * 20)
        user.addStatusEffect(StatusEffectInstance(StatusEffects.INVISIBILITY, duration, 0, true, false, true))
        return UseResult.success()
    }

    private fun spawnClones(player: ServerPlayerEntity, amount: Int, moveTime: Int) {
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
                    player.getRotationVector(0f, adjustedAngle),
                    moveTime
                ).apply {
                    this.yaw = adjustedAngle
                    headYaw = adjustedAngle
                })
        }
    }

    private fun createCloneEntity(player: ServerPlayerEntity, horizontalRotation: Vec3d, moveTime: Int): ClonePlayerEntity {
        return ClonePlayerEntity(player.world, player).apply {
            moveVelocity = horizontalRotation * (player.velocity.length() * 2.0).coerceAtMost(1.0)
            this.moveTime = moveTime
            if (player.velocity.y > 0) {
                jumpControl.setActive()
                setJumping(true)
            }
        }
    }
}