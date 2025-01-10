package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.mixin.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class GlidingSkill : PassiveSkill(
    Settings(
        id = "gliding",
        types = listOf(SkillType.MOVEMENT),
        rarity = SkillRarity.RARE
    )
), TickTrigger, JumpStateTrigger, ProgressTrigger {

    init {
        settings.addParameter(
            name = PARAM_GLIDE_DURATION,
            baseValue = DEFAULT_GLIDE_DURATION,
            enhancementId = ENHANCEMENT_DURATION,
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT
        )
    }

    override fun tick(player: PlayerEntity, usedTime: Int) {
        if (!player.isUsing()) {
            val data = player.getPersistentData()
            val time = data.getInt(NBT_REMAINING_TIME)
            if (player.isGliding()) {
                if (player.fallDistance > player.safeFallDistance / 2f && time > 0) {
                    player.startUsing()
                }
            } else if (player.canResetGliding()) {
                val totalTime = player.getTotalGlidingTime()
                if (time < totalTime) {
                    val newTime = (time + (totalTime / 50).coerceAtLeast(1)).coerceAtMost(totalTime)
                    data.putInt(NBT_REMAINING_TIME, newTime)
                    if (newTime == totalTime) {
                        player.syncData()
                    }
                }
            }
        }

        if (player.isUsing()) {
            val data = player.getPersistentData()
            val time = data.getInt(NBT_REMAINING_TIME)
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

                data.putInt(NBT_REMAINING_TIME, time - 1)
            }

            if (data.getInt(NBT_REMAINING_TIME) <= 0) {
                data.remove(NBT_REMAINING_TIME)
                player.stopUsing()
            }
        }
    }

    private fun PlayerEntity.isGliding(): Boolean =
        !isOnGround && !abilities.flying && !isTouchingWater && !isClimbing && (this as LivingEntityAccessor).isJumping

    private fun PlayerEntity.canResetGliding(): Boolean =
        isOnGround || abilities.flying || isTouchingWater || isClimbing

    fun PlayerEntity.getTotalGlidingTime() = getIntParam(PARAM_GLIDE_DURATION, this, DEFAULT_GLIDE_DURATION)

    override fun shouldDisplay(player: PlayerEntity): Boolean =
        player.isUsing() || player.getPersistentData().getInt(NBT_REMAINING_TIME) < player.getTotalGlidingTime()

    override fun getProgress(player: PlayerEntity): Double =
        player.getPersistentData().getInt(NBT_REMAINING_TIME) / player.getTotalGlidingTime().toDouble()

    override fun canBeEmpty(player: PlayerEntity): Boolean = true

    companion object {

        // NBT Keys
        private const val NBT_REMAINING_TIME = "RemainingTime"  // 剩余时间

        // Default Values
        private const val DEFAULT_GLIDE_DURATION = 5 * 20

        // Parameter Names
        private const val PARAM_GLIDE_DURATION = "glide_duration"  // 滑翔时长

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应滑翔时长
    }
}