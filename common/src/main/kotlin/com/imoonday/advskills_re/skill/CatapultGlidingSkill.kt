package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.item.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class CatapultGlidingSkill : LongPressSkill(
    id = "catapult_gliding",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 30,
    rarity = Rarity.RARE,
    sound = ModSounds.DASH
) {

    override fun getMaxPressTime(): Int = 3 * 20

    override fun use(user: ServerPlayerEntity): UseResult =
        if (!canUse(user)) failedResult() else if (user.isFallFlying) fallFlyingResult() else super.use(user)

    override fun onPress(player: ServerPlayerEntity): UseResult =
        if (!canUse(player)) failedResult() else if (player.isFallFlying) fallFlyingResult() else super.onPress(player)

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        if (!canUse(player)) return failedResult()
        if (player.isFallFlying) return fallFlyingResult()
        player.stopAndCooldown()
        player.playSkillSound()
        player.setOnGround(false)
        player.startFallFlying()
        player.velocityDirty = true
        val progress = pressedTime.toDouble() / getMaxPressTime()
        player.velocity =
            player.rotationVector.normalize().multiply(1.5 * progress, 0.0, 1.5 * progress)
                .withAxis(Direction.Axis.Y, 3.0 * progress)
        player.sendPacket(EntityVelocityUpdateS2CPacket(player))
        return UseResult.success()
    }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        if (player.isUsing() && !canUse(player)) player.stopUsing()
        super.serverTick(player, usedTime)
    }

    private fun failedResult() = UseResult.fail(failedMessage())

    private fun fallFlyingResult() = UseResult.fail(message("fallFlying"))

    private fun canUse(player: ServerPlayerEntity): Boolean {
        val stack = player.getEquippedStack(EquipmentSlot.CHEST)
        return (stack.item is ElytraItem && ElytraItem.isUsable(stack))
    }
}