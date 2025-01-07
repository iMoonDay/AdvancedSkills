package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.mixin.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class WallJumpSkill : PassiveSkill(
    Settings(
        id = "wall_jump",
        types = listOf(SkillType.MOVEMENT),
        cooldown = 0,
        rarity = SkillRarity.SUPERB
    )
), AutoTrigger, SendPlayerDataTrigger, UsingProgressTrigger, FallTrigger, StopTrigger {

    init {
        settings
            .addParameter(PARAM_JUMP_SOUND, DEFAULT_JUMP_SOUND)
            .addParameter(
                name = PARAM_JUMP_FORCE,
                baseValue = DEFAULT_JUMP_FORCE,
                enhancementId = ENHANCEMENT_FORCE,
                value = 0.1,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun shouldStart(player: ServerPlayerEntity): Boolean =
        player.getPersistentData().getBoolean(NBT_JUMP_TRIGGERED)

    override fun shouldStop(player: ServerPlayerEntity): Boolean {
        if (player.abilities.flying) {
            player.getPersistentData().remove(NBT_WALL_JUMPED)
            return true
        }
        return player.isOnGround
    }

    override fun postStop(player: PlayerEntity) {
        super.postStop(player)
        player.getPersistentData().remove(NBT_JUMP_TRIGGERED)
    }

    override fun tick(player: ServerPlayerEntity) {
        super.tick(player)
        if (!player.isUsing()) return

        if (shouldStart(player)) {
            val pos = player.eyePos.offset(player.horizontalFacing, player.width / 2.0 + 0.1).toBlockPos()
            val world = player.world
            val colliding = (!world.getBlockState(pos).isAir || !world.getBlockState(pos.down()).isAir)
            if (colliding) {
                jump(player)
                player.playSoundFromParam(PARAM_JUMP_SOUND, DEFAULT_JUMP_SOUND.get())
                val data = player.getPersistentData()
                data.remove(NBT_JUMP_TRIGGERED)
                data.putBoolean(NBT_WALL_JUMPED, true)
            } else {
                player.sendPacket(EntityPositionS2CPacket(player))
                player.updateVelocity()
            }
        }
    }

    private fun jump(player: PlayerEntity) {
        player.isSprinting = false
        player.jump()
        val jumpForce = getDoubleParam(PARAM_JUMP_FORCE, player, DEFAULT_JUMP_FORCE)
        player.velocity -= Vec3d.of(player.horizontalFacing.vector) * 0.25 * jumpForce
        player.abilities.flying = false
    }

    override fun onFall(amount: Int, player: ServerPlayerEntity, fallDistance: Float, damageMultiplier: Float): Int {
        val data = player.getPersistentData()
        return if (data.getBoolean(NBT_WALL_JUMPED)) {
            data.remove(NBT_WALL_JUMPED)
            amount / 2
        } else {
            amount
        }
    }

    override fun write(player: PlayerEntity, data: NbtCompound): NbtCompound {
        val jumped = hasJumped(player)
        data.putBoolean(NBT_JUMP_TRIGGERED, jumped)
        if (jumped) {
            jump(player)
        }
        return data
    }

    override fun getSendTime(): SendTime = SendTime.PREDICATE

    override fun shouldSendData(player: PlayerEntity): Boolean = player is ServerPlayerEntity || hasJumped(player)

    private fun hasJumped(player: PlayerEntity): Boolean {
        val pos = player.eyePos.offset(player.horizontalFacing, player.width / 2.0 + 0.1).toBlockPos()
        val jumping = (player as LivingEntityAccessor).isJumping && !player.isOnGround && !player.abilities.flying
        val colliding = player.horizontalCollision && jumping &&
            (!player.world.getBlockState(pos).isAir || !player.world.getBlockState(pos.down()).isAir)
        return jumping && colliding
    }

    override fun getProgress(player: PlayerEntity): Double = if (player.isUsing()) 1.0 else 0.0

    companion object {

        // Default Values
        private const val DEFAULT_JUMP_FORCE = 1.0
        private val DEFAULT_JUMP_SOUND = ModSounds.DASH

        // Parameter Names
        private const val PARAM_JUMP_SOUND = "jump_sound"  // 跳跃音效
        private const val PARAM_JUMP_FORCE = "jump_force"  // 跳跃力度

        // Enhancement IDs
        private const val ENHANCEMENT_FORCE = "force"  // 对应力度

        // NBT Keys
        private const val NBT_JUMP_TRIGGERED = "JumpTriggered"  // 跳跃触发
        private const val NBT_WALL_JUMPED = "WallJumped"  // 墙跳完成
    }
}