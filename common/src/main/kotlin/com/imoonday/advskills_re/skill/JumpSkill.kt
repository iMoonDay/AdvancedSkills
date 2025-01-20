package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class JumpSkill : PassiveSkill(
    Settings(
        id = "jump",
        types = listOf(SkillType.MOVEMENT),
        cooldown = 2,
        rarity = SkillRarity.COMMON
    ),
    toggleable = true
), TickTrigger, UsingProgressTrigger, JumpStateTrigger, StopTrigger, FallFlyingTrigger {

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings
            .addParameter(
                name = PARAM_JUMP_FORCE,
                baseValue = DEFAULT_JUMP_FORCE,
                enhancementId = ENHANCEMENT_FORCE,
                value = 0.35,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 3,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_JUMP_COUNT,
                baseValue = DEFAULT_JUMP_COUNT,
                enhancementId = ENHANCEMENT_COUNT,
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 4,
                descArg = Enhancement.ArgFormatter.INT
            )
    }

    override fun writeStartUsingData(user: ServerPlayerEntity): NbtCompound? {
        val jumpCount = getIntParam(PARAM_JUMP_COUNT, user, DEFAULT_JUMP_COUNT)
        if (jumpCount > 0) {
            return NbtCompound().apply { putInt(NBT_REMAINING_COUNT, jumpCount) }
        }
        return null
    }

    override fun onDeactivated(user: ServerPlayerEntity) {
        super.onDeactivated(user)
        user.startCooling()
    }

    override fun onJumped(player: PlayerEntity, onGround: Boolean) {
        super.onJumped(player, onGround)
        val remainingCount = player.getRemainingCount()
        if (remainingCount <= 0 || !canJump(player, onGround)) {
            return
        }

        player.run {
            setRemainingCount(remainingCount - 1)
            stopFallFlying()
            fallDistance = 0.0f
            jump()
            val force = getDoubleParam(PARAM_JUMP_FORCE, player, DEFAULT_JUMP_FORCE)
            velocity = velocity.multiply(1.0, force, 1.0)
            for (i in 0 until 10) {
                world.addParticle(
                    ParticleTypes.CLOUD,
                    x + random.nextGaussian() * 0.5,
                    y,
                    z + random.nextGaussian() * 0.5,
                    random.nextGaussian() * 0.1,
                    random.nextGaussian() * 0.1,
                    random.nextGaussian() * 0.1
                )
            }
        }
    }

    private fun canJump(player: PlayerEntity, onGround: Boolean): Boolean =
        !(player.isCooling() || onGround || player.abilities.flying || player.isFallFlying || !isAvailable(player))

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)

        val available = isAvailable(player)
        if (player.isOnGround || !available) {
            val jumpCount = getIntParam(PARAM_JUMP_COUNT, player, DEFAULT_JUMP_COUNT)
            if (jumpCount > 0) {
                val remainingCount = player.getRemainingCount()
                if (available && remainingCount != jumpCount) {
                    player.setRemainingCount(jumpCount)
                    if (remainingCount < jumpCount) {
                        player.startCooling()
                    } else {
                        player.syncData()
                    }
                }
            }
        }
    }

    override fun canStartFallFlying(player: PlayerEntity): Boolean {
        val remainingCount = player.getRemainingCount()
        return remainingCount <= 0 || !canJump(player, player.isOnGround)
    }

    override fun shouldDisplay(player: PlayerEntity): Boolean =
        isAvailable(player) && !player.isCooling()

    override fun isInUsingState(player: PlayerEntity): Boolean =
        shouldDisplay(player) && player.getRemainingCount() < getIntParam(PARAM_JUMP_COUNT, player, DEFAULT_JUMP_COUNT)

    override fun getProgress(player: PlayerEntity): Double {
        val remainingCount = player.getRemainingCount()
        return (remainingCount.toDouble() / getIntParam(
            PARAM_JUMP_COUNT, player, DEFAULT_JUMP_COUNT
        ).toDouble()).coerceIn(0.0, 1.0)
    }

    override fun canBeEmpty(player: PlayerEntity): Boolean = true

    private fun PlayerEntity.getRemainingCount(): Int = getActiveData().getInt(NBT_REMAINING_COUNT)

    private fun PlayerEntity.setRemainingCount(count: Int) = getActiveData().putInt(NBT_REMAINING_COUNT, count)

    companion object {

        // Default Values
        private const val DEFAULT_JUMP_FORCE = 1.0
        private const val DEFAULT_JUMP_COUNT = 1

        // NBT Keys
        private const val NBT_REMAINING_COUNT = "RemainingCount"

        // Parameter Names
        private const val PARAM_JUMP_FORCE = "jump_force"  // 跳跃力度
        private const val PARAM_JUMP_COUNT = "jump_count"  // 跳跃次数

        // Enhancement IDs
        private const val ENHANCEMENT_FORCE = "force"  // 对应跳跃力度
        private const val ENHANCEMENT_COUNT = "count"  // 对应跳跃次数
    }
}