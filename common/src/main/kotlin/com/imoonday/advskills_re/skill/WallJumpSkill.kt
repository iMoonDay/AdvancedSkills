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
), AutoTrigger, SendPlayerDataTrigger, UsingProgressTrigger, FallTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter("jump_sound", ModSounds.DASH)
            .addParameter(
                name = "jump_power",
                baseValue = 1.0,
                enhancementId = "power",
                value = 0.1,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun shouldStart(player: ServerPlayerEntity): Boolean = player.getPersistentData().getBoolean("jumped")

    override fun shouldStop(player: ServerPlayerEntity): Boolean {
        if (player.abilities.flying) {
            player.getPersistentData().remove("wallJumped")
            return true
        }
        return player.isOnGround
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
                player.playSoundFromParam("jump_sound", ModSounds.DASH.get())
                val data = player.getPersistentData()
                data.remove("jumped")
                data.putBoolean("wallJumped", true)
            } else {
                player.sendPacket(EntityPositionS2CPacket(player))
                player.updateVelocity()
            }
        }
    }

    private fun jump(player: PlayerEntity) {
        player.isSprinting = false
        player.jump()
        player.velocity -= Vec3d.of(player.horizontalFacing.vector) * 0.25
        val power = getDoubleParam("jump_power", player, 1.0)
        player.velocity = player.velocity.multiply(1.0, power, 1.0)
        player.abilities.flying = false
    }

    override fun onFall(amount: Int, player: ServerPlayerEntity, fallDistance: Float, damageMultiplier: Float): Int {
        val data = player.getPersistentData()
        return if (data.getBoolean("wallJumped")) {
            data.remove("wallJumped")
            amount / 2
        } else {
            amount
        }
    }

    override fun write(player: PlayerEntity, data: NbtCompound): NbtCompound {
        val jumped = hasJumped(player)
        data.putBoolean("jumped", jumped)
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
}