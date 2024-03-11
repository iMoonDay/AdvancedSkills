package com.imoonday.skill

import com.imoonday.init.ModSounds
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.send
import com.imoonday.util.translateSkill
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ElytraItem
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Direction

class CatapultGlidingSkill : LongPressSkill(
    id = "catapult_gliding",
    types = arrayOf(SkillType.MOVEMENT),
    cooldown = 30,
    rarity = Rarity.RARE,
    sound = ModSounds.DASH
) {

    override fun getMaxPressTime(): Int = 20 * 3

    override fun use(user: ServerPlayerEntity): UseResult =
        if (!canUse(user)) failedResult() else if (user.isFallFlying) fallFlyingResult() else super.use(user)

    override fun onPress(player: ServerPlayerEntity): UseResult =
        if (!canUse(player)) failedResult() else if (player.isFallFlying) fallFlyingResult() else super.onPress(player)

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        if (!canUse(player)) return failedResult()
        if (player.isFallFlying) return fallFlyingResult()
        player.stopUsing()
        player.startCooling()
        playSound(player)
        player.setOnGround(false)
        player.startFallFlying()
        player.velocityDirty = true
        val progress = pressedTime.toDouble() / getMaxPressTime()
        player.velocity =
            player.rotationVector.normalize().multiply(1.5 * progress, 0.0, 1.5 * progress)
                .withAxis(Direction.Axis.Y, 3.0 * progress)
        player.send(EntityVelocityUpdateS2CPacket(player))
        return UseResult.success()
    }

    override fun tick(player: ServerPlayerEntity, usedTime: Int) {
        if (player.isUsing() && !canUse(player)) player.stopUsing()
        super.tick(player, usedTime)
    }

    private fun failedResult() = UseResult.fail(translateSkill(id.path, "failed"))

    private fun fallFlyingResult() = UseResult.fail(translateSkill(id.path, "fallFlying"))

    private fun canUse(player: ServerPlayerEntity): Boolean {
        val stack = player.getEquippedStack(EquipmentSlot.CHEST)
        return (stack.item is ElytraItem && ElytraItem.isUsable(stack))
    }
}