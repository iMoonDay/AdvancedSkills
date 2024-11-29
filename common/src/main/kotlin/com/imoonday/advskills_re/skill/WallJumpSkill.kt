package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.mixin.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class WallJumpSkill : PassiveSkill(
    id = "wall_jump",
    extraTypes = listOf(SkillType.MOVEMENT),
    cooldown = 0,
    rarity = Rarity.SUPERB,
    sound = ModSounds.DASH
), AutoTrigger, SendPlayerDataTrigger, UsingProgressTrigger, FallTrigger {

    override fun shouldStart(player: ServerPlayerEntity): Boolean = player.properties.getBoolean("jumped")

    override fun shouldStop(player: ServerPlayerEntity): Boolean = player.isOnGround

    override fun tick(player: ServerPlayerEntity) {
        super.tick(player)
        if (!player.isUsing()) return
        if (shouldStart(player)) {
            val pos = player.eyePos.offset(player.horizontalFacing, player.width / 2.0 + 0.1).toBlockPos()
            val world = player.world
            val colliding = (!world.getBlockState(pos).isAir || !world.getBlockState(pos.down()).isAir)
            if (colliding) {
                jump(player)
                player.playSkillSound()
                player.properties.putBoolean("wallJumped", true)
            } else {
                player.sendPacket(EntityPositionS2CPacket(player))
                player.sendPacket(EntityVelocityUpdateS2CPacket(player))
            }
        }
    }

    private fun jump(player: PlayerEntity) {
        player.isSprinting = false
        player.jump()
        player.velocity -= Vec3d.of(player.horizontalFacing.vector) * 0.25
        player.abilities.flying = false
    }

    override fun onFall(amount: Int, player: ServerPlayerEntity, fallDistance: Float, damageMultiplier: Float): Int {
        val properties = player.properties
        return if (properties.getBoolean("wallJumped")) {
            properties.remove("wallJumped")
            amount / 2
        } else {
            amount
        }
    }

    override fun write(player: PlayerEntity, data: NbtCompound): NbtCompound {
        val pos = player.eyePos.offset(player.horizontalFacing, player.width / 2.0 + 0.1).toBlockPos()
        val jumping = (player as LivingEntityAccessor).isJumping
        val colliding = player.horizontalCollision && jumping &&
            (!player.world.getBlockState(pos).isAir || !player.world.getBlockState(pos.down()).isAir)
        val jumped = jumping && colliding
        data.putBoolean("jumped", jumped)
        if (jumped) {
            jump(player)
        }
        return data
    }

    override fun apply(player: ServerPlayerEntity, data: NbtCompound) =
        player.properties.putBoolean("jumped", data.getBoolean("jumped"))

    override fun getSendTime(): SendTime = SendTime.EQUIPPED

    override fun getProgress(player: PlayerEntity): Double = if (player.isUsing()) 1.0 else 0.0
}