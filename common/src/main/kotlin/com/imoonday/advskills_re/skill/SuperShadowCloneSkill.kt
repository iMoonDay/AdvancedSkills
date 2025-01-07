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

    init {
        settings
            .addParameter(PARAM_CLONE_MOVE_TIME, DEFAULT_CLONE_MOVE_TIME)
            .addParameter(
                name = PARAM_CLONE_COUNT,
                baseValue = DEFAULT_CLONE_COUNT,
                enhancementId = ENHANCEMENT_COUNT,
                value = 2,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = PARAM_STEALTH_DURATION,
                baseValue = DEFAULT_STEALTH_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        val cloneCount = getIntParam(PARAM_CLONE_COUNT, user, DEFAULT_CLONE_COUNT)
        val moveTime = getIntParam(PARAM_CLONE_MOVE_TIME, user, DEFAULT_CLONE_MOVE_TIME)
        spawnClones(user, cloneCount, moveTime)
        val stealthDuration = getIntParam(PARAM_STEALTH_DURATION, user, DEFAULT_STEALTH_DURATION)
        user.addStatusEffect(StatusEffectInstance(StatusEffects.INVISIBILITY, stealthDuration, 0, true, false, true))
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

    companion object {
        // Default Values
        private const val DEFAULT_CLONE_MOVE_TIME = 5 * 20
        private const val DEFAULT_CLONE_COUNT = 8
        private const val DEFAULT_STEALTH_DURATION = 5 * 20

        // Parameter Names
        private const val PARAM_CLONE_MOVE_TIME = "clone_move_time"  // 分身移动时间
        private const val PARAM_CLONE_COUNT = "clone_count"  // 分身数量
        private const val PARAM_STEALTH_DURATION = "stealth_duration"  // 隐身持续时间

        // Enhancement IDs
        private const val ENHANCEMENT_COUNT = "count"  // 对应数量
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
    }
}