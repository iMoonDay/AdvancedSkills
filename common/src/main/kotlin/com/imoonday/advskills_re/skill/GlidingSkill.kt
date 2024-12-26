package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.mixin.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

private const val REMAINING_TIME_KEY = "remainingTime"

class GlidingSkill : PassiveSkill(
    id = "gliding",
    extraTypes = listOf(SkillType.MOVEMENT),
    rarity = SkillRarity.RARE,
    enhancements = setOf(SkillEnhancements.PERSISTENT_TIME)
), TickTrigger, JumpStateTrigger, ProgressTrigger {

    init {
        addEnhanceableParameter(
            "gliding_time",
            5 * 20,
            "time",
            0.2f,
            Enhancement.Type.MULTIPLY,
            5
        ) { (it * 100).toInt() }
    }

    override fun tick(player: PlayerEntity, usedTime: Int) {
        if (!player.isUsing()) {
            val data = player.getPersistentData()
            val time = data.getInt(REMAINING_TIME_KEY)
            if (player.isGliding()) {
                if (player.fallDistance > player.safeFallDistance / 2f && time > 0) {
                    player.startUsing()
                }
            } else if (player.canResetGliding()) {
                val totalTime = player.getTotalGlidingTime()
                if (time < totalTime) {
                    val newTime = (time + (totalTime / 50).coerceAtLeast(1)).coerceAtMost(totalTime)
                    data.putInt(REMAINING_TIME_KEY, newTime)
                    if (newTime == totalTime) {
                        player.syncData()
                    }
                }
            }
        }

        if (player.isUsing()) {
            val data = player.getPersistentData()
            val time = data.getInt(REMAINING_TIME_KEY)
            if (!player.isGliding() || time <= 0) {
                player.stopUsing()
                return
            }

            val velocity = player.velocity
            if (velocity.y < 0.0) {
                player.velocity = velocity.withAxis(Direction.Axis.Y, (velocity.y * 0.75).coerceAtLeast(-1.0))
                player.velocityDirty = true
                player.fallDistance = 0.0f
                if (usedTime % 5 == 0) {
                    val delta = player.width / 2.0
                    (player as? ServerPlayerEntity)?.spawnParticles(
                        ParticleTypes.END_ROD,
                        false, player.pos, 5,
                        delta, 0.0, delta, 0.0
                    )
                }
            }

            data.putInt(REMAINING_TIME_KEY, time - 1)
            if (data.getInt(REMAINING_TIME_KEY) <= 0) {
                data.remove(REMAINING_TIME_KEY)
                player.stopUsing()
            }
        }
    }

    private fun PlayerEntity.isGliding(): Boolean =
        !isOnGround && !abilities.flying && !isTouchingWater && !isClimbing && (this as LivingEntityAccessor).isJumping

    private fun PlayerEntity.canResetGliding(): Boolean =
        isOnGround || abilities.flying || isTouchingWater || isClimbing

    fun PlayerEntity.getTotalGlidingTime() = getIntParameter("gliding_time")

    override fun shouldDisplay(player: PlayerEntity): Boolean =
        player.isUsing() || player.getPersistentData().getInt(REMAINING_TIME_KEY) < player.getTotalGlidingTime()

    override fun getProgress(player: PlayerEntity): Double =
        player.getPersistentData().getInt(REMAINING_TIME_KEY) / player.getTotalGlidingTime().toDouble()

    override fun canBeEmpty(player: PlayerEntity): Boolean = true
}